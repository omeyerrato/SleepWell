"""Verify isolated migrations before opening the updated personal app database.

Keeps a private app-data archive and installed APK, never uninstalls or clears data.
If isolated tests fail before migration, restores the previous APK in place.
"""
from pathlib import Path
import argparse, datetime, io, json, sqlite3, subprocess, tarfile

parser=argparse.ArgumentParser()
parser.add_argument('--adb',required=True)
parser.add_argument('--serial',required=True)
args=parser.parse_args()
root=Path(__file__).resolve().parents[1]
adb=[args.adb,'-s',args.serial]
backup=root/'.device-backups'/('storage-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))
backup.mkdir(parents=True,exist_ok=False)
def run(*parts,**kwargs):return subprocess.run(adb+list(parts),check=True,**kwargs)
def archive(name):
    blob=run('exec-out','run-as','org.sleepwell.app','tar','-cf','-','.',capture_output=True).stdout
    (backup/name).write_bytes(blob)
    with tarfile.open(fileobj=io.BytesIO(blob)) as tar:
        return {m.name:tar.extractfile(m).read() for m in tar.getmembers() if m.isfile()}
def database(files,label):
    folder=backup/label;folder.mkdir()
    for suffix in ('','-wal','-shm'):
        key='./databases/sleepwell.db'+suffix
        if key in files:(folder/('sleepwell.db'+suffix)).write_bytes(files[key])
    return sqlite3.connect((folder/'sleepwell.db').as_uri()+'?mode=ro',uri=True)
before=archive('private-before.tar')
with database(before,'before-db') as connection:
    version=connection.execute('PRAGMA user_version').fetchone()[0]
    original={}
    for table in ('alarms','sessions','epochs','clips'):
        columns=[r[1] for r in connection.execute('PRAGMA table_info('+table+')')]
        original[table]=(columns,connection.execute('SELECT '+','.join(columns)+' FROM '+table+' ORDER BY id').fetchall())
installed=run('shell','pm','path','org.sleepwell.app',capture_output=True,text=True).stdout.strip().splitlines()
assert len(installed)==1 and installed[0].startswith('package:/data/app/'),'Expected one installed base APK'
run('pull',installed[0][8:],str(backup/'original.apk'))
passed=False
try:
    run('install','-r',str(root/'app/build/outputs/apk/debug/app-debug.apk'))
    run('install','-r',str(root/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'))
    result=run('shell','am','instrument','-w','-r','-e','class','org.sleepwell.app.StorageMigrationTest,org.sleepwell.app.RecordStorageTest,org.sleepwell.app.RecordReplayTest,org.sleepwell.app.RecordClockTest','org.sleepwell.app.test/androidx.test.runner.AndroidJUnitRunner',capture_output=True,text=True,encoding='utf-8',errors='replace')
    (backup/'test-result.txt').write_text(result.stdout+result.stderr,encoding='utf-8')
    print(result.stdout)
    if 'OK (' not in result.stdout:raise RuntimeError('Isolated migration tests failed; personal database must not be opened deliberately')
    passed=True
finally:
    if not passed:
        current=archive('private-after-failure.tar')
        with database(current,'failure-db') as connection:current_version=connection.execute('PRAGMA user_version').fetchone()[0]
        if current_version==version:run('install','-r',str(backup/'original.apk'))
        else:print('Database already migrated; kept current APK to avoid incompatible downgrade.')
    run('shell','am','start','-W','-n','org.sleepwell.app/.MainActivity','--ez','visualReview','true')
if passed:
    # start -W above waits for the newly migrated Activity to launch.
    after=archive('private-after.tar')
    with database(after,'after-db') as connection:
        assert connection.execute('PRAGMA user_version').fetchone()[0]==4
        for table,(columns,rows) in original.items():
            assert rows==connection.execute('SELECT '+','.join(columns)+' FROM '+table+' ORDER BY id').fetchall(),table+' data changed'
        assert connection.execute('PRAGMA quick_check').fetchone()[0]=='ok'
    print('Migration passed; all original alarm/session/epoch/clip column values preserved; SQLite integrity ok.')

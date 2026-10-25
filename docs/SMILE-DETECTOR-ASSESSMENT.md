# F029 Smile detector assessment — not shipped

No Smile task, detector dependency, camera flow or placeholder was added by this assessment. F029 remains missing. Research and a fixed-policy host evaluation are not Android detector verification.

## Primary-source choices

Google’s [bundled ML Kit face detector](https://developers.google.com/ml-kit/vision/face-detection/android) exposes an actual smiling probability and an immediately available offline model. Its [terms/privacy notice](https://developers.google.com/ml-kit/terms) restrict source/model extraction and disclose API metrics traffic. It was not adopted as an open model or a silent proprietary fallback.

MediaPipe’s library source is Apache-2.0, but code licensing alone does not establish the exact downloadable model bundle’s redistribution terms. Its published task privacy notice also describes metrics. That additional model/telemetry verification was not completed within this increment.

OpenCV’s [official Android Maven distribution](https://opencv.org/opencv4android-usage-models/) provides a fully offline route. The [runtime is Apache-2.0](https://opencv.org/license/); the original [frontal-face](https://raw.githubusercontent.com/opencv/opencv/4.12.0/data/haarcascades/haarcascade_frontalface_default.xml) and [smile classifier](https://raw.githubusercontent.com/opencv/opencv/4.12.0/data/haarcascades/haarcascade_smile.xml) XML headers expressly permit redistribution subject to retaining their BSD-style notices. The proposed 4.12.0 AAR is large (about 112 MB for all packaged ABIs). No AAR was added, no ABIs removed, and APK size was not measured. Cascade weights are raw classifier scores, not calibrated smile probabilities.

## Fixed policy declared before evaluation

- Grayscale image, maximum long edge 640 pixels.
- Frontal face: scale factor 1.1, minimum neighbors 5, minimum 96×96 pixels, raw level weight at least 2.0. Exactly one accepted face, centered in the middle 60% of each image dimension.
- Smile: lower-face ROI x=5–95%, y=45–95%; scale factor 1.1, minimum neighbors 8, minimum 36×18 pixels; raw level weight at least 1.5, smile width at most 80% of face width.
- Proposed live hold: at least eight fresh admitted frames spanning 1,500 ms, at most 400 ms between accepted frames, stable geometry IoU at least 0.6; misses, multiple faces, stale frames and pause reset the hold. Maximum five analyses per second, one latest frame, 60-second visible attempt with explicit retry or Math fallback.

The first evaluation used one fixed licensed portrait, with no positive-image or threshold search. Pete Souza’s 6 December 2012 official White House portrait, [LOC item 2017645540](https://www.loc.gov/pictures/item/2017645540/), has the Library of Congress rights advisory “No known restrictions on publication.” [NARA describes official-duty photographs in the corresponding White House photographer collection as public domain](https://www.obamalibrary.gov/digital-research-room/finding-aids/photographs-taken-white-house-photographer-pete-souza-24-13193). No identity or endorsement claim is made.

Pinned inputs:

| Input | Source | SHA-256 |
| --- | --- | --- |
| Frontal classifier | OpenCV tag 4.12.0, XML linked above | `0f7d4527844eb514d4a4948e822da90fbb16a34a0bbbbc6adc6498747a5aafb0` |
| Smile classifier | OpenCV tag 4.12.0, XML linked above | `4ca1f304eabd0b5ae30180c81acb53e166a5867e5be17b316bd3f32cfdf87d8a` |
| Portrait | `https://cdn.loc.gov/service/pnp/ppbd/00600/00603v.jpg` | `1a46b4b0d7bd00f7b02feabadf84fded2e920285c854e948a2fdfb7c2f44ac2f` |

Host runtime: temporary `opencv-python-headless==4.12.0.88`, reporting OpenCV 4.12.0. Source portrait dimensions were 820×1024; maximum-long-edge downsampling used OpenCV `INTER_AREA` and produced 512×640. No histogram equalization, recropping or other positive-image preprocessing was introduced.

## Actual result

The frontal detector returned rectangle `[179, 50, 148, 148]` with raw level weight `5.9609081480884925`. Its vertical center was `124 / 640 = 0.19375`, below the predeclared lower centering bound `0.2`. The fixed policy therefore accepted **zero** faces and the positive case was **false**. A uniform gray frame generated at the same dimensions produced zero faces as expected. Because face admission failed, the smile stage was not executed; no smile-inference success is claimed.

The image or thresholds were not revised after this failure, and no alternative positive fixture was hunted. This result establishes insufficient evidence for the proposed complete flow, not proof that the cascade cannot detect smiles. Proceeding later requires a separately reviewed evaluation protocol and broader licensed positive/negative evidence before shipping.

A future implementation would also need Android inference verification, all existing ABIs preserved (including S23 arm64 and CI x86_64), explicit front-camera consent/error/denial/Math fallback, actual on-device lifecycle tests where authorized, owned CameraX use-case cleanup, and exact run/unit proof freshness. A classical smile cascade cannot establish identity or liveness and may react to printed faces; lighting, pose, glasses, occlusion and demographic variation can affect detection. No general accuracy claim or verified user-camera behavior follows from this assessment.

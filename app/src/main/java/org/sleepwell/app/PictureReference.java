package org.sleepwell.app;
/** Compact reference only; visual descriptors can still reveal scene characteristics. */
final class PictureReference {final String label;final PictureSignature signature;PictureReference(String label,PictureSignature signature){if(label==null||label.isBlank()||label.length()>60||label.codePoints().anyMatch(Character::isISOControl)||signature==null)throw new IllegalArgumentException("Name the picture and capture a detailed reference first.");this.label=label.strip();this.signature=signature;}String key(){return signature.key();}}

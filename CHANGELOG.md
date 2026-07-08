# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The changes documented here do not include those from the original repository.

## [1.0.2]

### 2026-07-07

- Fix: explicitly grant `FLAG_GRANT_READ_URI_PERMISSION` (in addition to the existing write permission) when launching camera capture intents, required for Android 18's implicit URI grant restriction ([RMET-5241](https://outsystemsrd.atlassian.net/browse/RMET-5241)).

## [1.0.1]

### 2026-04-27

- Fix: gallery multiple selection returning duplicate `uri` and `webPath` on Android devices where the MediaStore `_data` column is unavailable. Each selected image now gets a unique cache filename via `UUID.randomUUID()`.

## [1.0.0]

### 2026-04-10

- Add implementation for whole library, including `takePhoto`, `recordVideo`, `deleteVideoFilesFromCache`, `createCaptureFile`, `processResultFromCamera`, `processResultFromVideo`, `editImage`, `editURIPicture`, `openCropActivity`, `processResultFromEdit`, `chooseFromGallery`, `onChooseFromGalleryResult`, `onChooseFromGalleryEditResult`, `extractUris`, and `playVideo`.
- The methods above are spread into different managers with differenr responsibilities: `IONCAMRCameraManager`, `IONCAMREditManager`, `IONCAMRGalleryManager` and `IONCAMRVideoManager`.

### 2026-02-19

- Create repository.


# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The changes documented here do not include those from the original repository.

## [1.0.0]

### 2026-04-10

- Add implementation for whole library, including `takePhoto`, `recordVideo`, `deleteVideoFilesFromCache`, `createCaptureFile`, `processResultFromCamera`, `processResultFromVideo`, `editImage`, `editURIPicture`, `openCropActivity`, `processResultFromEdit`, `chooseFromGallery`, `onChooseFromGalleryResult`, `onChooseFromGalleryEditResult`, `extractUris`, and `playVideo`.
- The methods above are spread into different managers with differenr responsibilities: `IONCAMRCameraManager`, `IONCAMREditManager`, `IONCAMRGalleryManager` and `IONCAMRVideoManager`.

### 2026-02-19

- Create repository.


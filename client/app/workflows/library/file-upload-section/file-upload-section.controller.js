require('./file-upload-section.less');

class FileUploadSectionController {
  /* @ngInject */
  constructor(LibraryService) {
    this.LibraryService = LibraryService;
  }

  openFileBrowser() {
    document.getElementById('uploader-input').click();
  }

}

export default FileUploadSectionController;

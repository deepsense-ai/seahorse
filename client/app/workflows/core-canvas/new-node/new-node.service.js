const ELEMENT_PLACEHOLDER = '<div class="canvas__new-element">New Element Placeholder</div>';

class NewNodeService {
  constructor() {
    'ngInject';
    this.isWizardActive = false;
  }

  initialize(container) {
    this.$container = $(container);
  }

  startWizard(x, y) {
    this.resetWizard();
    this.$wizardElement = $(ELEMENT_PLACEHOLDER);
    this.$wizardElement.css('left', x);
    this.$wizardElement.css('top', y);
    this.$container.append(this.$wizardElement);
    this.isWizardActive = true;
  }

  resetWizard() {
    if (this.isWizardActive === true) {
      this.$container[0].removeChild(this.$wizardElement[0]);
    }
  }
}

export default NewNodeService;

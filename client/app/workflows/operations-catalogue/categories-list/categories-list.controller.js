const CATALOG_MENU_WIDTH = 230;
const SINGLE_CATALOG_MENU_ITEM_HEIGHT = 35;
const OPERATION_MENU_WIDTH = 250;
const OPERATION_MENU_HEIGHT = 175;

class CategoriesListController {
  constructor($element, $timeout) {
    'ngInject';
    this.$element = $element;
    this.$timeout = $timeout;
  }

  $onChanges() {
    this.$timeout(() => {
      this.childrenPlacement = [];
      this.offset = $(this.$element[0]).offset();
      this.categories.forEach((category, idx) => {
        this.childrenPlacement[idx] = this.getPositions(category, idx);
      });
    });
  }

  getPositions(category, indexOnMenu) {
    const WIDTH = this.containment[0].offsetWidth;
    const HEIGHT = this.containment[0].offsetHeight;

    const catalogMenuHeight = (indexOnMenu + 1) * SINGLE_CATALOG_MENU_ITEM_HEIGHT;
    const numberOfCategories = category.catalog.length;
    const catalogMenuToShowHeight = numberOfCategories * SINGLE_CATALOG_MENU_ITEM_HEIGHT;

    const canPutCatalogOnRight = WIDTH - this.offset.left - CATALOG_MENU_WIDTH > CATALOG_MENU_WIDTH;
    const canPutOperationsOnRight = WIDTH - this.offset.left - OPERATION_MENU_WIDTH > OPERATION_MENU_WIDTH;

    const canPutCatalogOnBottom = HEIGHT - this.offset.top - catalogMenuHeight > catalogMenuToShowHeight;
    const canPutOperationsOnBottom = HEIGHT - this.offset.top - catalogMenuHeight > OPERATION_MENU_HEIGHT;

    const defaultTopShift = '-8px';
    const topShiftForCatalogMenu = HEIGHT - (this.offset.top + (indexOnMenu + 1 + numberOfCategories) * SINGLE_CATALOG_MENU_ITEM_HEIGHT) + 'px';
    const topShiftForOperationsMenu = HEIGHT - (OPERATION_MENU_HEIGHT + this.offset.top + catalogMenuHeight) + 'px';

    const catalogTopShift = canPutCatalogOnBottom ? defaultTopShift : topShiftForCatalogMenu;
    const operationsTopShift = canPutOperationsOnBottom ? defaultTopShift : topShiftForOperationsMenu;

    return {
      catalog: {
        right: canPutCatalogOnRight,
        bottom: canPutCatalogOnBottom,
        topShift: catalogTopShift
      },
      operations: {
        right: canPutOperationsOnRight,
        bottom: canPutOperationsOnBottom,
        topShift: operationsTopShift
      }
    };
  }
}

export default CategoriesListController;

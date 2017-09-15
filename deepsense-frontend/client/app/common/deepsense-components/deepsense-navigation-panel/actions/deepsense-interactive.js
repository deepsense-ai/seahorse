/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

(function() {
  'use strict';

  const DEFAULT_MAX_ZOOM_RATIO = 2.0;
  const DEFAULT_ID = 'unsetID';

  // ratios for scrolling
  const ZOOM_IN_RATIO_SCROLL = 1.2;
  const ZOOM_OUT_RATIO_SCROLL = 0.85;

  // ratios for clicking at the zooming buttons
  const ZOOM_IN_RATIO_DEFAULT = 1.2;
  const ZOOM_OUT_RATIO_DEFAULT = 0.85;

  // callbacks for handling zoom in/out events will be invoked 100 ms after the last invocation
  const DEBOUNCE_TIMEOUT = 100;

  // sets the zoom to exactly this ratio
  const STRICT = true;

  class NavigationService {
    constructor(params) {
      this.state = {
        originX: 0,
        originY: 0,
        totalScale: 1
      };

      this.$interactivePanel = params.$interactivePanel;
      this.$interactiveElement = params.$interactiveElement;
      this.$document = params.$document;
      this.$rootScope = params.$rootScope;
      this.$timeout = params.$timeout;

      this.$interactivePanel.addClass('deepsense-interactive-panel');
      this.$interactiveElement.addClass('deepsense-interactive-element');

      this.activeMoving = false;
      this.fitTimes = 1;

      let getElWidth = ($el) => $el[0].getBoundingClientRect().width;
      let getElHeight = ($el) => $el[0].getBoundingClientRect().height;
      let initInteractiveElWidth = getElWidth(this.$interactiveElement);
      let initInteractiveElHeight = getElHeight(this.$interactiveElement);
      let interactivePanelWidth = getElWidth(this.$interactivePanel);
      let interactivePanelHeight = getElHeight(this.$interactivePanel);

      this._internal = {
        MIN_ZOOM_RATIO: Math.max(interactivePanelWidth / initInteractiveElWidth, interactivePanelHeight / initInteractiveElHeight),
        MAX_ZOOM_RATIO: params.maxZoomRatio || DEFAULT_MAX_ZOOM_RATIO,
        getZoomPanelWidth: () => interactivePanelWidth,
        getZoomPanelHeight: () => interactivePanelHeight,
        getInteractiveElWidth: () => initInteractiveElWidth * this.state.totalScale,
        getInteractiveElHeight: () => initInteractiveElHeight * this.state.totalScale
      };
    }

    updateTransformStyle(animatedTransition = false) {
      this.$interactiveElement.css({
        'transform': `translate(${this.state.originX}px, ${this.state.originY}px) scale(${this.state.totalScale})`,
        'transition': 'transform ' + (animatedTransition ? '0.1s' : '0s')
      });
    }

    adjustCorners() {
      // TODO: comments

      if (this._internal.getInteractiveElWidth() < this._internal.getZoomPanelWidth()) {
        /*
         * If the width of the $interactiveElement is not greater than the width of the $interactivePanel,
         * the interactiveElement should be stuck to the left edge of the $interactivePanel.
         */
        this.state.originX = 0;
      } else {
        this.state.originX = Math.min(this.state.originX, 0);
      }

      if (this._internal.getInteractiveElHeight() < this._internal.getZoomPanelHeight) {
        /*
         * Analogously, the heights.
         */
        this.state.originY = 0;
      } else {
        this.state.originY = Math.min(this.state.originY, 0);
      }

      if (this.state.originX + this._internal.getInteractiveElWidth() < this._internal.getZoomPanelWidth()) {
        this.state.originX = this._internal.getZoomPanelWidth() - this._internal.getInteractiveElWidth();
      }

      if (this.state.originY + this._internal.getInteractiveElHeight() < this._internal.getZoomPanelHeight()) {
        this.state.originY = this._internal.getZoomPanelHeight() - this._internal.getInteractiveElHeight();
      }
    }

    movePanel(transition, animatedTransition = true) {
      this.state.originX += transition.left;
      this.state.originY += transition.top;

      this.adjustCorners();
      this.updateTransformStyle(animatedTransition);
    }

    performZooming(relativeX, relativeY, zoomRatio, isStrict = false) {
      let newTotalScale = isStrict ? zoomRatio : this.state.totalScale * zoomRatio;
      newTotalScale = Math.max(this._internal.MIN_ZOOM_RATIO, newTotalScale);
      newTotalScale = Math.min(this._internal.MAX_ZOOM_RATIO, newTotalScale);

      // TODO: comments
      this.state = {
        originX: relativeX - ((relativeX - this.state.originX) / this.state.totalScale) * newTotalScale,
        originY: relativeY - ((relativeY - this.state.originY) / this.state.totalScale) * newTotalScale,
        totalScale: newTotalScale
      };

      this.adjustCorners();
      //disable animation when performing strict zoom to fix Firefox issue with fit calculations
      this.updateTransformStyle(!isStrict);
    }

    performCenterZooming(zoomRatio, isStrict) {
      let relativeX = this.$interactivePanel.width() / 2;
      let relativeY = this.$interactivePanel.height() / 2;

      this.performZooming(relativeX, relativeY, zoomRatio, isStrict);
    }

    handleMouseDown(e, mouseMoveHandler, mouseUpHandler) {
      if (this.activeMoving) {
        e.preventDefault();

        this.$interactiveElement.toggleClass('deepsense-moving');
        this.$document.on('mousemove', mouseMoveHandler);
        this.$document.on('mouseup', mouseUpHandler);
      }
    }

    handleMouseMove(e) {
      e.preventDefault();

      this.movePanel({
        left: e.movementX,
        top: e.movementY
      }, false);
    }

    handleMouseUp(e, mouseMoveHandler, mouseUpHandler) {
      e.preventDefault();

      this.$interactiveElement.toggleClass('deepsense-moving');
      this.$document.off('mousemove', mouseMoveHandler);
      this.$document.off('mouseup', mouseUpHandler);
    }

    fit (collection, collectionItemDimensions) {
      /**
       * Get data phase
       */

      /**
       * visibleFrame.left and visibleFrame.top are "0 position". Such position
       * from which centers' difference is calculated.
       *
       * zoom-state: constant.
       */
      let visibleFrame = {
        left: this.$interactivePanel.offset().left,
        top: this.$interactivePanel.offset().top,
        height: this.$interactivePanel.outerHeight(),
        width: this.$interactivePanel.outerWidth()
      };
      let visibleFrameCenter = {
        x: visibleFrame.width / 2,
        y: visibleFrame.height / 2
      };

      /**
       * zoom-state: constant (could change only x,y values).
       */
      let collectionItemsXCoordinates = jQuery.map(collection, item => item.x);
      let collectionItemsYCoordinates = jQuery.map(collection, item => item.y);

      /**
       * zoom-state: constant (could change only x,y values).
       */
      let margin = 50;
      let bottomMargin = 150;
      let pseudoRectangleOfVisibleCollectionItems = {
        left: Math.min.apply(Math, collectionItemsXCoordinates) - margin,
        top: Math.min.apply(Math, collectionItemsYCoordinates) - margin,
        right: Math.max.apply(Math, collectionItemsXCoordinates) + margin,
        bottom: Math.max.apply(Math, collectionItemsYCoordinates) + bottomMargin
      };
      // alias
      let psRect = pseudoRectangleOfVisibleCollectionItems;
      psRect.width = psRect.right - psRect.left +
        collectionItemDimensions.width;
      psRect.height = psRect.bottom - psRect.top +
        collectionItemDimensions.height;
      let psRectCenter = {
        x: psRect.width / 2 * this.state.totalScale,
        y: psRect.height / 2 * this.state.totalScale
      };

      /**
       * We need to create pseudo container as element in order to get its
       * absolute offset positions.
       */
      psRect.$element = $(`<div style="position: absolute;
                     left: ${psRect.left}px;
                     top: ${psRect.top}px;
                     width: ${psRect.width}px;
                     height: ${psRect.height}px;
                     background-color: rgba(0, 0, 0, 0.5);" />`)
        .appendTo(this.$interactiveElement);

      /**
       * Get data phase [end]
       */

      /**
       * Calculation phase
       */

      /**
       * Taking position of pseudo container into "0 position"
       */
      let pseudoCenterOffset = {
        x: visibleFrame.left - psRect.$element.offset().left,
        y: visibleFrame.top - psRect.$element.offset().top
      };

      /**
       * Calculate new center. Now is related to "0 position".
       */
      let zoom = Math.min(
        visibleFrame.height / psRect.height,
        visibleFrame.width / psRect.width
      );

      psRectCenter.x = psRectCenter.x - pseudoCenterOffset.x;
      psRectCenter.y = psRectCenter.y - pseudoCenterOffset.y;

      let relativeX = visibleFrameCenter.x - psRectCenter.x;
      let relativeY = visibleFrameCenter.y - psRectCenter.y;

      //True added to disable animation when performing strict zoom to fix Firefox issue with fit calculations
      this.movePanel({
        top: relativeY,
        left: relativeX
      }, true);

      this.performCenterZooming(zoom, STRICT);

      /**
       * Remove pseudo container from DOM.
       */
      psRect.$element.remove();

      /**
       * Fix when alignment is not on center after first use
       * TODO rewrite this by finding a way in calculations
       * WARNING! timeout = 200 should be, because of animation in transform
       */
      this.$timeout(() => {
        if (this.fitTimes) {
          this.fitTimes -= 1;
          this.fit.apply(this, arguments);
        } else if (this.fitTimes === 0) {
          this.fitTimes = 1;
        }
      }, 200, false);
    }

    initToCenter () {
      this.movePanel({
        left: -(this.$interactiveElement.width() / 2),
        top: -(this.$interactiveElement.height() / 2)
      });
    }
  }

  /*@ngInject*/
  function DeepsenseInteractive($document, $rootScope, $timeout, debounce) {
    return {
      restrict: 'A',
      scope: {
        'zoomId': '@',
        'maxZoomRatio': '=',
        'collection': '=',
        'collectionItemDimensions': '=',
        'autorun': '=deepsenseInteractiveAutorun'
      },
      link: (scope, element) => {
        scope.zoomId = scope.zoomId || DEFAULT_ID;
        scope.$applyAsync(() => {
          const $interactiveElement = $(element[0]);
          const $interactivePanel = $interactiveElement.parent();

          let navigationServiceInstance = new NavigationService({
            maxZoomRatio: scope.maxZoomRatio,
            $interactivePanel,
            $interactiveElement,
            $document,
            $rootScope,
            $timeout
          });
          let emitZoomEvent = () => {
            scope.$emit('ZOOM.ZOOM_PERFORMED', {
              zoomRatio: navigationServiceInstance.state.totalScale
            });
          };
          let emitFitEvent = () => {
            scope.$emit('FIT.FIT_PERFORMED', {
              zoomRatio: navigationServiceInstance.state.totalScale
            });
          };
          let wheelHandler = debounce((e) => {
            // (cursorX, cursorY) is the position of the cursor against (!) the $interactivePanel
            let cursorX = e.originalEvent.clientX - $interactivePanel[0].getBoundingClientRect().left;
            let cursorY = e.originalEvent.clientY - $interactivePanel[0].getBoundingClientRect().top;

            if (e.originalEvent.deltaY > 0) {
              // rolling up
              navigationServiceInstance.performZooming(cursorX, cursorY, ZOOM_OUT_RATIO_SCROLL);
            } else {
              // rolling down
              navigationServiceInstance.performZooming(cursorX, cursorY, ZOOM_IN_RATIO_SCROLL);
            }

            emitZoomEvent();
          }, DEBOUNCE_TIMEOUT, true);

          $interactivePanel.bind('wheel', (e) => {
            e.preventDefault();
            wheelHandler(e);
          });

          scope.$on('INTERACTION-PANEL.ZOOM-OUT', (_, data) => {
            if (scope.zoomId === data.zoomId) {
              navigationServiceInstance.performCenterZooming(ZOOM_OUT_RATIO_DEFAULT);
              emitZoomEvent();
            }
          });

          scope.$on('INTERACTION-PANEL.ZOOM-IN', (_, data) => {
            if (scope.zoomId === data.zoomId) {
              navigationServiceInstance.performCenterZooming(ZOOM_IN_RATIO_DEFAULT);
              emitZoomEvent();
            }
          });

          scope.$on('INTERACTION-PANEL.MOVE-GRAB', (_, data) => {
            if (scope.zoomId === data.zoomId) {
              navigationServiceInstance.activeMoving = data.active;
              navigationServiceInstance.$interactiveElement.toggleClass('deepsense-moveable');
            }
          });

          scope.$on('INTERACTION-PANEL.FIT', (_, data) => {
            if (scope.zoomId === data.zoomId && !jQuery.isEmptyObject(scope.collection)) {
              navigationServiceInstance.fit(scope.collection, scope.collectionItemDimensions);
              emitFitEvent();
            }
          });

          let mouseMoveHandler = (e) => { navigationServiceInstance.handleMouseMove(e); };
          let mouseUpHandler = (e) => { navigationServiceInstance.handleMouseUp(e, mouseMoveHandler, mouseUpHandler); };
          $interactiveElement.on('mousedown', (e) => {
            navigationServiceInstance.handleMouseDown(e, mouseMoveHandler, mouseUpHandler);
          });

          if (scope.autorun && !jQuery.isEmptyObject(scope.collection)) {
            scope.$emit('INTERACTION-PANEL.FIT', {
              zoomId: scope.zoomId
            });
          } else {
            navigationServiceInstance.initToCenter();
          }
        });
      }
    };
  }

  angular.module('deepsense.navigation-panel').
    directive('deepsenseInteractive', DeepsenseInteractive);
})();

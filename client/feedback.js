'use strict';

var _ues = {
  host:'deepsense.userecho.com',
  forum:'45446',
  lang:'en',
  tab_icon_show:false,
  tab_corner_radius:0,
  tab_font_size:20,
  tab_image_hash:'ZmVlZGJhY2s%3D',
  tab_chat_hash:'Y2hhdA%3D%3D',
  tab_alignment:'top',
  tab_text_color:'#ffffff',
  tab_text_shadow_color:'#00000055',
  tab_bg_color:'#00b1eb',
  tab_hover_color:'#5ba0dd'
};

(function() {
  var _ue = document.createElement('script'); _ue.type = 'text/javascript';
  _ue.async = true;
  _ue.src = ('https:' == document.location.protocol ? 'https://' : 'http://')
    + 'cdn.userecho.com/js/widget-1.4.gz.js';
  var s = document.getElementsByTagName('script')[0];
  _ue.onload = function () {
    UE.Popin.preload();
  };
  s.parentNode.insertBefore(_ue, s);
})();

(function cache () {
  var FEEDBACK_WINDOW_TIMEOUT = 1000;
  var timeout;
  var closeTimeout;
  var hasCookie = function () {
    "use strict";

    return document.cookie.indexOf('feedback-closed=1') > 0;
  };
  var showFeedbackWindow = function showFeedbackWindow () {
      if (!hasCookie()) {
        UE.Popin.show();

        listenToClose().done(setCookie);
      }
    };
  var listenToClose = function listenToClose () {
      var def = new $.Deferred();

      closeTimeout = setTimeout(function waiter () {
        if ($('#ue-dlg').css('display') === 'none') {
          def.resolve();
        } else {
          closeTimeout = setTimeout(waiter, 300);
        }
      }, 300);

      return def.promise();
    };
  var setCookie = function setCookie () {
      clearTimeout(closeTimeout);

      document.cookie = 'feedback-closed=1;expires=' +
        moment().year(moment().year() + 1).toDate().toUTCString();
    };
  var blurListener = function blurListener () {
      clearTimeout(timeout);

      showFeedbackWindow();
    };

  timeout = setTimeout(showFeedbackWindow, FEEDBACK_WINDOW_TIMEOUT);

  window.addEventListener('blur', blurListener);
})();

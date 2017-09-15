'use strict';

/* jshint ignore:start */
window._ues = {
  host: 'deepsense.userecho.com',
  forum: '45446',
  lang: 'en',
  tab_icon_show: false,
  tab_corner_radius: 0,
  tab_font_size: 20,
  tab_image_hash: 'ZmVlZGJhY2s%3D',
  tab_chat_hash: 'Y2hhdA%3D%3D',
  tab_alignment: 'left',
  tab_text_color: '#ffffff',
  tab_text_shadow_color: '#00000055',
  tab_bg_color: '#00b1eb',
  tab_hover_color: '#5ba0dd'
};

(function () {
  var _ue = document.createElement('script');
  _ue.type = 'text/javascript';
  _ue.async = true;
  _ue.src = ('https:' === document.location.protocol ? 'https://' : 'http://') + 'cdn.userecho.com/js/widget-1.4.gz.js';
  var s = document.getElementsByTagName('script')[0];
  _ue.onload = function () {
    UE.Popin.preload();
    var oldClose = UE.Dialog.close;
    UE.Dialog.close = function () {
      window.focus();
      oldClose.apply(this, arguments);
    };
  };
  -s.parentNode.insertBefore(_ue, s);
})();

/* jshint ignore:end */

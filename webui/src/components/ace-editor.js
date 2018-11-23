var ace = require('brace');

module.exports = {
  template: '<div :style="{height: height, width: width}"></div>',

  props: {
    value: {
      type:String,
      twoWay:true,
      required:true
    },
    lang: {
      type: String,
      default: 'text'
    },
    theme: {
      type: String,
      default: 'chrome'
    },
    height: {
      type: String,
      default: '300px'
    },
    width: {
      type: String,
      default: '100%'
    },
    readonly: {
      type: Boolean,
      default: false
    },
    options: {
      type: Object,
      default: function () { return {}; }
    }
  },

  data: function () {
    return {
      editor: null,
    };
  },

  mounted: function () {
    var vm = this;
    var lang = vm.lang;
    var theme = vm.theme;
    var editor = vm.editor = ace.edit(vm.$el);
    var options = vm.options;
    var readonly = vm.readonly;
    editor.$blockScrolling = Infinity;
    editor.getSession().setMode('ace/mode/' + lang);
    editor.setTheme('ace/theme/' + theme);
    editor.setValue(vm.value, 1);
    editor.setOptions(options);
    editor.setReadOnly(readonly);
    editor.on('change', function () {
      vm.value = editor.getValue();
    });
  },

  watch: {
    value: function (newValue) {
      this.editor.setValue(newValue, 1);
    },

    theme: function (newTheme) {
      this.editor.setTheme('ace/theme/' + newTheme);
    }
  }
};
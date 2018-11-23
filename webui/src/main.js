import Vue from 'vue'
import App from './App'

import Element from 'element-ui'
import './styles/element-variables.scss'
import VueRouter from 'vue-router'
import store from './vuex/store'
import routes from './routes'
import 'font-awesome/css/font-awesome.min.css'

Vue.filter('formatDate', (value) => {
  return new Date(value).format('yyyy-MM-dd hh:mm:ss');
});

Vue.use(Element, {
  size: 'small'
});
Vue.use(VueRouter);

const router = new VueRouter({
  routes
});

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app');


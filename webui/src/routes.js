import NotFound from './views/404.vue'
import NotPerm from './views/403.vue'
import Main from './views/Main.vue'
import Publish from './views/Publish.vue'
import Subscribe from './views/Subscribe.vue'
import ServerStatus from './views/ServerStatus.vue'

let routes = [
  {
    path: '/',
    redirect: '/publish',//设置默认指向的路径
    name: 'publish'
  },
  {
    path: '/',
    component: Main,
    name: '',
    hidden: true,
    children: [
      { path: '/403', component: NotPerm, name: '403' }
    ]
  },
  {
    path: '/',
    component: Main,
    name: '',
    icon: 'fa fa-user',
    leaf: true,//只有一个节点
    children: [
      { path: '/publish', component: Publish, name: 'Publish'}
    ]
  },
  {
    path: '/',
    component: Main,
    name: '',
    icon: 'fa fa-user',
    leaf: true,//只有一个节点
    children: [
      { path: '/subscribe', component: Subscribe, name: 'Subscribe'}
    ]
  },
  {
    path: '/',
    component: Main,
    name: '',
    icon: 'fa fa-user',
    leaf: true,//只有一个节点
    children: [
      { path: '/status', component: ServerStatus, name: 'ServerStatus'}
    ]
  },
  {
    path: '*',
    hidden: true,
    redirect: { path: '/404' }
  }
];

export default routes;

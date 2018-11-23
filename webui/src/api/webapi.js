import axios from 'axios';

export default {
  getCurrentUser() {
    return axios.get('staff/getCurrentUser');
  },
  listStaff(params) {
    return axios.post('staff/listStaff', params);
  },
  getUserByEmail(email) {
    return axios.get('staff/getUserByEmail', {params: { email: email }});
  },
  createStaff(params) {
    return axios.post('staff/createStaff', params);
  },
  listRolesByEmail(email) {
    return axios.get('staff/listRolesByEmail', {params: { email: email }});
  },
  assignRoleToUser(params) {
    return axios.post('staff/assignRoleToUser', params);
  },
  listConfig(params) {
    return axios.post('config/listConfig', params);
  },
  createConfig(params) {
    return axios.post('config/createConfig', params);
  },
  updateConfig(params) {
    return axios.post('config/updateConfig', params);
  },
  listRobot(params) {
    return axios.post('robot/listRobot', params);
  },
  createRobot(params) {
    return axios.post('robot/createRobot', params);
  },
  updateRobot(params) {
    return axios.post('robot/updateRobot', params);
  },
  resetRobot(robotId) {
    return axios.get('robot/resetRobot', {params: { robotId: robotId }});
  },
  listCabinet(robotId) {
    return axios.get('cabinet/listCabinet', {params: { robotId: robotId }});
  },
  listPoi(params) {
    return axios.post('poi/listPoi', params);
  },
  createPoi(params) {
    return axios.post('poi/createPoi', params);
  },
  updatePoi(params) {
    return axios.post('poi/updatePoi', params);
  },
  listDeliveryTask(params) {
    return axios.post('task/listDeliveryTask', params);
  },
  listDeliveryOrder(params) {
    return axios.post('task/listDeliveryOrder', params);
  },
  listDeliveryOrderEvent(deliveryOrderId) {
    return axios.get('task/listDeliveryOrderEvent', {params: { deliveryOrderId: deliveryOrderId }});
  },
  listDeliveryTaskEvent(deliveryTaskId) {
    return axios.get('task/listDeliveryTaskEvent', {params: { deliveryTaskId: deliveryTaskId }});
  },
  listMovementTaskEvent(movementTaskId) {
    return axios.get('task/listMovementTaskEvent', {params: { movementTaskId: movementTaskId }});
  },
  relocationToPoi(robotId) {
    return axios.post('robot/relocationToPoi', {robotId: robotId, poiCode: 101});
  },

  listStaffPoiMapping(params) {
    return axios.post('staffPoiMapping/list', params);
  },

  updateStaffPoiMapping(params) {
    return axios.post('staffPoiMapping/update', params);
  },

  createStaffPoiMapping(params) {
    return axios.post('staffPoiMapping/create', params);
  },

  listBuildingFloorPoi(params) {
      return axios.get('staffPoiMapping/listBuildingFloorPoi', params);
  },

  listBuilding(params) {
    return axios.post('building/list', params);
  },

  updateBuilding(params) {
    return axios.post('building/update', params);
  },

  createBuilding(params) {
    return axios.post('building/create', params);
  },

  listRobotMap(params) {
    return axios.post('map/list', params);
  },
  createRobotMap(params) {
    return axios.post('map/addRobotMap', params);
  },
  updateRobotMap(params) {
    return axios.post('map/updateRobotMap', params);
  },
  getMapUrlByHash(fileHash) {
    return axios.get('map/getMapUrlByHash', {params: {fileHash: fileHash}});
  },
  totalInfo() {
    return axios.get('/robot/totalInfo');
  }
}

<template>
    <div class="card-panel" @click="handleSetLineChartData('newVisitis')">
        <div class="card-panel-icon-wrapper" :style="{ color: color}">
            <i class="card-panel-icon" :class="icon"></i>
        </div>
        <div class="card-panel-description">
            <div class="card-panel-text">{{text}}</div>
            <div class="card-panel-num">{{num}}</div>
        </div>
    </div>
</template>

<script>
  export default {
    props: {
      text: String,
      num: String,
      color: String,
      icon: String
    }
  }
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
    .card-panel {
        height: 108px;
        font-size: 12px;
        position: relative;
        overflow: hidden;
        color: #666;
        background: #fff;
        box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
        border-color: rgba(0, 0, 0, .05);
        .icon-people {
            color: #40c9c6;
        }
        .icon-message {
            color: #36a3f7;
        }
        .icon-money {
            color: #f4516c;
        }
        .icon-shoppingCard {
            color: #34bfa3
        }
        .card-panel-icon-wrapper {
            float: left;
            margin: 14px 0 0 14px;
            padding: 16px;
            transition: all 0.38s ease-out;
            border-radius: 6px;
        }
        .card-panel-icon {
            float: left;
            font-size: 48px;
        }
        .card-panel-description {
            float: right;
            font-weight: bold;
            margin: 26px 26px 26px 0px;
            .card-panel-text {
                line-height: 18px;
                color: rgba(0, 0, 0, 0.45);
                font-size: 16px;
                margin-bottom: 12px;
            }
            .card-panel-num {
                font-size: 20px;
            }
        }
    }
</style>

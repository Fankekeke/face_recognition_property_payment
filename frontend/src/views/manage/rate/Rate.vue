<template>
  <div hoverable :bordered="false" style="width: 100%;margin-top: 25px">
    <a-row :gutter="20">
      <a-col :span="8">
        <a-select v-model="queryDate" style="width: 100%" showSearch optionFilterProp="children" @change="housesChange">
          <a-select-option v-for="(item, index) in housesList" :key="index" :value="item.id">{{ item.address }}</a-select-option>
        </a-select>
      </a-col>
      <a-col :span="1">
        <a-button type="primary" @click="selectRate">
          查询
        </a-button>
      </a-col>
    </a-row>
    <a-card :bordered="false" style="width: 100%;margin-top: 50px">
      <a-skeleton active v-if="chartLoading" />
      <p v-if="!chartLoading" style="font-weight: 650;font-size: 15px;margin-bottom: 8px;font-family: SimHei">本年用电统计</p>
      <apexchart v-if="!chartLoading" type="area" height="350" :options="chartOptions" :series="series"></apexchart>
    </a-card>
    <a-card :bordered="false" style="width: 100%;">
      <a-skeleton active v-if="chartLoading" />
      <p v-if="!chartLoading" style="font-weight: 650;font-size: 15px;margin-bottom: 8px;font-family: SimHei">本年用水统计</p>
      <apexchart v-if="!chartLoading" type="area" height="350" :options="chartOptions3" :series="series3"></apexchart>
    </a-card>
    <a-card :bordered="false" style="width: 100%">
      <a-skeleton active v-if="chartLoading" />
      <p v-if="!chartLoading" style="font-weight: 650;font-size: 15px;margin-bottom: 8px;font-family: SimHei">本年应缴水电费用统计</p>
      <apexchart v-if="!chartLoading" type="bar" height="300" :options="chartOptions1" :series="series1"></apexchart>
    </a-card>
    <a-card hoverable :bordered="false" style="width: 100%">
      <a-skeleton active v-if="chartLoading" />
      <p v-if="!chartLoading" style="font-weight: 650;font-size: 15px;margin-bottom: 8px;font-family: SimHei">本年实缴费用统计</p>
      <apexchart  v-if="!chartLoading" type="line" height="300" :options="chartOptions2" :series="series2"></apexchart>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'rate',
  data () {
    return {
      queryDate: null,
      chartLoading: false,
      housesList: [],
      series: [{
        name: '用电量',
        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      }],
      chartOptions: {
        chart: {
          height: 350,
          type: 'area'
        },
        dataLabels: {
          enabled: false
        },
        stroke: {
          curve: 'smooth'
        },
        xaxis: {
          categories: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12']
        },
        tooltip: {
        }
      },
      series1: [{
        name: '应缴',
        data: []
      }],
      chartOptions1: {
        chart: {
          type: 'bar',
          height: 300
        },
        title: {
          text: '',
          align: 'left'
        },
        plotOptions: {
          bar: {
            horizontal: false,
            columnWidth: '55%'
          }
        },
        dataLabels: {
          enabled: false
        },
        stroke: {
          show: true,
          width: 2,
          colors: ['transparent']
        },
        xaxis: {
          categories: []
        },
        yaxis: {
          title: {
            text: ''
          }
        },
        fill: {
          opacity: 1
        },
        tooltip: {
          y: {
            formatter: function (val) {
              return val + ' 元'
            }
          }
        }
      },
      series2: [{
        name: '实际',
        data: []
      }],
      chartOptions2: {
        chart: {
          type: 'line',
          height: 300
        },
        xaxis: {
          categories: []
        },
        stroke: {
          curve: 'stepline'
        },
        dataLabels: {
          enabled: false
        },
        title: {
          text: '',
          align: 'left'
        },
        markers: {
          hover: {
            sizeOffset: 4
          }
        }
      },
      series3: [{
        name: '用水量',
        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      }],
      chartOptions3: {
        chart: {
          height: 350,
          type: 'area'
        },
        dataLabels: {
          enabled: false
        },
        stroke: {
          curve: 'smooth'
        },
        xaxis: {
          categories: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12']
        },
        tooltip: {
        }
      }
    }
  },
  mounted () {
    this.getHousesList()
  },
  methods: {
    housesChange (value, option) {
      this.housesList.forEach(item => {
        if (item.id === value) {
          this.housesData = item
        }
      })
      this.selectRate(value)
    },
    getHousesList () {
      this.$get('/cos/houses-info/list').then((r) => {
        this.housesList = r.data.data
      })
    },
    selectRate (houseId) {
      this.chartLoading = true
      this.$get('/cos/payment-manage/queryPaymentByHouseId', {houseId}).then((r) => {
        if (r.data.data1) {
          this.series[0].data = r.data.data1.map(obj => { return obj.value })
          this.chartOptions.xaxis.categories = r.data.data1.map(obj => { return obj.name })
        }
        if (r.data.data3) {
          this.series1[0].data = r.data.data3.map(obj => { return obj.value })
          this.chartOptions1.xaxis.categories = r.data.data3.map(obj => { return obj.name })
        }
        if (r.data.data4) {
          this.series2[0].data = r.data.data4.map(obj => { return obj.value })
          this.chartOptions2.xaxis.categories = r.data.data4.map(obj => { return obj.name })
        }
        if (r.data.data2) {
          this.series3[0].data = r.data.data2.map(obj => { return obj.value })
          this.chartOptions3.xaxis.categories = r.data.data3.map(obj => { return obj.name })
        }
        this.chartLoading = false
      })
    }
  }
}
</script>

<style scoped>

</style>

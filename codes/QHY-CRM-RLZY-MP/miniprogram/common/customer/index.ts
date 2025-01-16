import { apiRequest } from '../../api/ApiRequest'
import staticData from "../../utils/static-data"

import { areaList } from '../../miniprogram_npm/@vant/area-data/data';
Page({
  data: {
    name:"",
    phone:'',
    phoneError:'',
    type:'',
    typeName:'',
    pname:'',
    pid:null,
    showType:false,
    columnsType:staticData.customerType,
    showCompany:false,
    listCustomer:[],
    showFirmSize:false,
    firmSizeName:'',
    firmSizeId:null,
    listFirmSize:staticData.firmSize,
    showRegion:false,
    listRegion:areaList,
    region:[],
    regionName:'',
    address:'',
    id:null
  },
  getName(event){
    this.setData({
      name:event.detail
    })
  },
  getPhone(event){
    var phone = event.detail.value.replace(/\D/g, '')
      console.log(event)
    this.setData({
      phone:phone
    })
    const regex = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
    if (phone.length !== 0 && phone.length !== 11) {
        this.setData({
            phoneError: '手机长度有误'
        })
    } else if (phone.length !== 0 && !regex .test(phone)) {
        this.setData({
            phoneError: '手机号格式有误'
        })
    }else{
        this.setData({
            phoneError: ''
        })
    }
  },
  getAddress(event){
    this.setData({
      address:event.detail
    })
  },
  listCustomerNotPage(){
    apiRequest.listCustomerNotPage('2').then((res)=>{
      if(res.code === 200){
        let data = [];
        console.log(res.data);
        for(let item of res.data){
          var obj = {text:item.name,value:item.id};
          data.push(obj);
        }
        this.setData({
          listCustomer:data
        })
      }
    })
  },
  showTypePopup(){
      if(this.data.id){
          return false
      }
    this.setData({
      showType:true
    })
  },
  closeTypePopup(){
    this.setData({
      showType:false
    })
  },
  cancelTypePopup(){
    this.setData({
      showType:false
    })
  },
  confirmTypePopup(event){
    this.setData({
      showType:false,
      type:event.detail.value.value,
      typeName:event.detail.value.text
    })
  },
  showCompanyPopup(){
    if(this.data.id && this.data.pname){
        return false
    }
    this.listCustomerNotPage()
    this.setData({
      showCompany:true
    })
  },
  closeCompanyPopup(){
    this.setData({
      showCompany:false
    })
  },
  cancelCompanyPopup(){
    this.setData({
      showCompany:false
    })
  },
  confirmCompanyPopup(event){
    this.setData({
      showCompany:false,
      pid:event.detail.value.value,
      pname:event.detail.value.text
    })
  },
  showFirmSizePopup(){
    this.setData({
      showFirmSize:true
    })
  },
  closeFirmSize(){
    this.setData({
      showFirmSize:false
    })
  },
  confirmFirmSize(event){
    this.setData({
      showFirmSize:false,
      firmSizeName:event.detail.value.text,
      firmSizeId:event.detail.value.value,
    })
  },
  showRegionPopup(){
    this.setData({
        showRegion:true
      })
  },
  closeRegion(){
    this.setData({
        showRegion:false
      })
  },
  confirmRegion(event){
    var val = event.detail.values;
    console.log(val);
    this.setData({
        region:val,
        regionName:event.detail.values[0].name + '-' + event.detail.values[1].name + '-' + event.detail.values[2].name,
        showRegion:false
    })
  },
  submit() {
    if(!this.data.name){
        wx.showToast({title: '请输入客户名称',icon: 'none',duration: 2000})
        return false;
    }
    if(!this.data.phone){
        wx.showToast({title: '请输入联系电话',icon: 'none',duration: 2000})
        return false;
    }else{
        const regex = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
        if (this.data.phone.length !== 0 && this.data.phone.length !== 11) {
            wx.showToast({title: '手机长度有误',icon: 'none',duration: 2000})
            return false;
        } else if (this.data.phone.length !== 0 && !regex .test(this.data.phone)) {
            wx.showToast({title: '手机号格式有误',icon: 'none',duration: 2000})
            return false;
        }
    }
    if(!this.data.type){
        wx.showToast({title: '请选择客户类型',icon: 'none',duration: 2000})
        return false;
    }else if(this.data.type === "3" && !this.data.pid){
        wx.showToast({title: '请选择集团客户',icon: 'none',duration: 2000})
        return false;
    }
    if(!this.data.firmSizeId){
        wx.showToast({title: '请选择客户规模',icon: 'none',duration: 2000})
        return false;
    }
    if(!this.data.regionName){
        wx.showToast({title: '请选择所属区域',icon: 'none',duration: 2000})
        return false;
    }
    if(!this.data.address){
        wx.showToast({title: '请输入详细地址',icon: 'none',duration: 2000})
        return false;
    }
    let data = {
        id:this.data.id?this.data.id:undefined,
        name:this.data.name,
        firmSize:this.data.firmSizeId,
        phone:this.data.phone,
        provinceCode:this.data.region[0].code,
        cityCode:this.data.region[1].code,
        areaCode:this.data.region[2].code,
        address:this.data.address,
        type:this.data.type,
        pid:this.data.type=== '3'?this.data.pid:undefined
    }
    if(this.data.id){
        apiRequest.updateCustomer(data).then((res)=>{
            if(res.code === 200){
                wx.showToast({title: res.msg,icon: 'none',duration: 2000});
                setTimeout(()=>{
                    let pages = getCurrentPages();
                    let beforePage = pages[pages.length -2];
                    beforePage.setData({
                        refreshCompany:true
                    })
                    wx.navigateBack({         //返回首页 
                        delta:1
                    })
                },2000)
            }
        })
    }else{
        apiRequest.addCustomer(data).then((res)=>{
            if(res.code === 200){
                wx.showToast({title: res.msg,icon: 'none',duration: 2000});
                setTimeout(()=>{
                    let pages = getCurrentPages();
                    let beforePage = pages[pages.length -3];
                    wx.navigateBack({         //返回首页 
                        delta:2
                    })
                },2000)
            }
        })
    }

  },
  // 根据客户id获取客户信息
  getCustomerInfoById(){
    apiRequest.getCustomerInfoById(this.data.id).then((res)=>{
        if(res.code === 200){
            let data = res.data;
            data.typeName = staticData.customerTypeOfStatus(data.type);
            data.firmSizeName = staticData.firmSizeOfStatus(data.firmSize);
            let region = [{code:data.provinceCode,name:data.crmProvince?.name},{code:data.cityCode,name:data.crmCity?.name},{code:data.areaCode,name:data.crmArea?.name}];
            this.setData({
                name:data.name,
                phone:data.phone,
                typeName:data.typeName,
                type:data.type,
                pname:data.pid?data.pcustomer.name:'',
                pid:data.pid?data.pcustomer.id:null,
                firmSizeName:data.firmSizeName,
                firmSizeId:data.firmSize,
                regionName:data.area,
                region:region,
                address:data.address
            })
        }
    })
  },
  onLoad(option) {
      if(option && option.name){
        this.setData({
            name:option.name
        })
      }
      if(option && option.id){
        wx.setNavigationBarTitle({
            title:'修改客户'
        });
        this.setData({
            id:option.id
        })
        this.getCustomerInfoById();
      }else{
        wx.setNavigationBarTitle({
            title:'添加客户'
        });
      }
    
  }
})

import { apiRequest } from '../../api/ApiRequest'
Page({

    /**
     * 页面的初始数据
     */
    data: {
        info:{},
        status:0,
        type:''
    },
    updateMsgStatus(){
        apiRequest.updateMsgStatus({id:this.data.info.id}).then((res)=>{
            
        })
    },
    goPostOne(){
        wx.navigateTo({
            url: '../../pages/postpone/index?id='+this.data.info.id+'&name='+this.data.info.customer.name
        })
    },
    // 营销客户接单
    getOrder(){
    var that = this
    wx.showModal({
        title: '',
        content: '接单后该客户将变为您和'+ that.data.info.mainSalerUser.userName +'的共有客户',
        confirmText:'认领客户',
        success (res) {
          if (res.confirm) {
            apiRequest.getOrder({cid:that.data.info.customer.id}).then((res)=>{
                if(res.code === 200){
                    wx.showToast({title: res.msg,icon: 'none',duration: 2000});
                    setTimeout(()=>{
                        let pages = getCurrentPages();
                        let beforePage = pages[pages.length -2];
                        wx.navigateBack({         //返回首页 
                            delta:1
                        })
                    },2000)
                }
            })
          }
        }
      })
    },
    goCustomer(){
        if(this.data.type !== '3'){
            return false;
        }
        wx.navigateTo({
            url:'../../pages/customerinfo/index?id='+this.data.info.customer.id+'&notice='+this.data.type
        })
    },
    onLoad(option) {
        if(option && option.obj && option.type){
            var info = JSON.parse(decodeURIComponent(option.obj));
            var type = option.type
            console.log("type",type)
            this.setData({
                info:info,
                status:info.customer.status,
                type:type
            })
            this.updateMsgStatus();
        }
    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady() {

    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {

    },

    /**
     * 生命周期函数--监听页面隐藏
     */
    onHide() {

    },

    /**
     * 生命周期函数--监听页面卸载
     */
    onUnload() {

    },

    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh() {

    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom() {

    },

    /**
     * 用户点击右上角分享
     */
    onShareAppMessage() {

    }
})
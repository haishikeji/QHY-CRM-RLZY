import { apiRequest } from '../../api/ApiRequest'
Page({
  data: {
    info:{},
    id:null
  },
  getApprovalDetail(){
    apiRequest.getApprovalDetail(this.data.id).then((res)=>{
        if(res.code === 200){
            this.setData({
                info:res.data
            })
        }
    })
  },
  onLoad(option) {
    wx.setNavigationBarTitle({
        title:'我的申请'
    })
    if(option && option.id){
        this.setData({
            id:option.id
        })
        this.getApprovalDetail()
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

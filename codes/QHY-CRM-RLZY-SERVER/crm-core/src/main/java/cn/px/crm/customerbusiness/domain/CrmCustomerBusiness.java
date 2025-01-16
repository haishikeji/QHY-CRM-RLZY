package cn.px.crm.customerbusiness.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.px.crm.business.domain.CrmBusiness;
import cn.px.crm.customer.domain.CrmCustomer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.px.common.annotation.Excel;
import cn.px.common.core.domain.BaseEntity;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 客户业务管理对象 crm_customer_business
 *
 * @author 品讯科技
 * @date 2024-08
 */
public class CrmCustomerBusiness extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 客户id */
    private Long cid;

    /** 业务类型id */
    private Long businessId;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    private Long uid;

    /** 人数 */
    @Excel(name = "人数",sort = 4,type = Excel.Type.IMPORT)
    @NotNull(message = "人数不能空")
    private Long peopleNum;


    public Long getStartNum() {
        return startNum;
    }

    public void setStartNum(Long startNum) {
        this.startNum = startNum;
    }
    @Excel(name = "月初人数",sort = 4,type = Excel.Type.EXPORT)
    private Long startNum;

    public Long getEndNum() {
        return endNum;
    }

    public void setEndNum(Long endNum) {
        this.endNum = endNum;
    }
    @Excel(name = "月末人数",sort = 5,type = Excel.Type.EXPORT)
    private Long endNum;

    private Long total;

    public Long getSubNum() {
        return subNum;
    }

    public void setSubNum(Long subNum) {
        this.subNum = subNum;
    }

    @Excel(name = "上月月初同比增减人数",sort = 7,type = Excel.Type.EXPORT)
    private Long subNum;

    public String getDivNum() {
        return divNum;
    }

    public void setDivNum(String divNum) {
        this.divNum = divNum;
    }

    @Excel(name = "上月月初同比增减比例",sort = 9,type = Excel.Type.EXPORT)
    private String divNum;

    public String getDivEndNum() {
        return divEndNum;
    }

    public void setDivEndNum(String divEndNum) {
        this.divEndNum = divEndNum;
    }
    @Excel(name = "上月月末同比增减比例",sort = 10,type = Excel.Type.EXPORT)
    private String divEndNum;

    @Excel(name = "单位名称",sort = 1)
    @NotEmpty(message = "单位名称不能空")
    private String cname;

    public Long getPreMonthEndNum() {
        return preMonthEndNum;
    }

    public void setPreMonthEndNum(Long preMonthEndNum) {
        this.preMonthEndNum = preMonthEndNum;
    }
    @Excel(name = "上月月末人数",sort = 6,type = Excel.Type.EXPORT)
    private Long preMonthEndNum;

    public Long getSubEndNum() {
        return subEndNum;
    }

    public void setSubEndNum(Long subEndNum) {
        this.subEndNum = subEndNum;
    }
    @Excel(name = "上月月末同比增减人数",sort = 8,type = Excel.Type.EXPORT)
    private Long subEndNum;

    public Long getPreMonthNum() {
        return preMonthNum;
    }

    public void setPreMonthNum(Long preMonthNum) {
        this.preMonthNum = preMonthNum;
    }
    @Excel(name = "上月月初人数",sort = 5,type = Excel.Type.EXPORT)
    private Long preMonthNum;

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getSalerName() {
        return salerName;
    }

    public void setSalerName(String salerName) {
        this.salerName = salerName;
    }

    @Excel(name = "业务员",sort = 3)
    @NotEmpty(message = "业务员不能为空")
    private String salerName;

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    @JsonFormat(pattern = "yyyy-MM")
    private Date addTime;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setCid(Long cid)
    {
        this.cid = cid;
    }

    public Long getCid()
    {
        return cid;
    }
    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    public Long getBusinessId()
    {
        return businessId;
    }
    public void setPeopleNum(Long peopleNum)
    {
        this.peopleNum = peopleNum;
    }

    public Long getPeopleNum()
    {
        return peopleNum;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    @Excel(name = "业务类型",sort = 2)
    @NotEmpty(message = "业务类型不能为空")
    private String businessName;

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    private String dateType;

    public CrmCustomer getCrmCustomer() {
        return crmCustomer;
    }

    public void setCrmCustomer(CrmCustomer crmCustomer) {
        this.crmCustomer = crmCustomer;
    }

    private CrmCustomer crmCustomer;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("cid", getCid())
            .append("businessId", getBusinessId())
            .append("peopleNum", getPeopleNum())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
    /*********************************/
    @Getter
    @Setter
    private Integer  day;
    @Getter
    @Setter
    private int  monthTime;
    @Getter
    @Setter
    private int  counts;
    @Getter
    @Setter
    private String  customerStatus;
    @Getter
    @Setter
    private List<String> customerStatusList;
    @Getter
    @Setter
    private CrmBusiness business;
    @Getter
    @Setter
    private Long deptId;
    @Getter
    @Setter
    private Long loginDeptId;
    @Getter
    @Setter
    private Date toTime;
    @Getter
    @Setter
    private String oper;
    @Getter
    @Setter
    @JsonFormat(pattern = "yyyy-MM")
    private Date toMonth;
    @Getter
    @Setter
    private String queryType;
    @Getter
    @Setter
    private String toManager;
    @Getter
    @Setter
    private String type;



}

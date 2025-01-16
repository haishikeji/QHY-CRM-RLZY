package cn.px.crm.salercustomer.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.px.common.annotation.Log;
import cn.px.common.core.controller.BaseController;
import cn.px.common.core.domain.AjaxResult;
import cn.px.common.enums.BusinessType;
import cn.px.crm.salercustomer.domain.CrmSalerCustomer;
import cn.px.crm.salercustomer.service.ICrmSalerCustomerService;
import cn.px.common.utils.poi.ExcelUtil;
import cn.px.common.core.page.TableDataInfo;

/**
 * 销售人员与客户中间Controller
 *
 * @author 品讯科技
 * @date 2024-08
 */
@RestController
@RequestMapping("/salercustomer/salercustomer")
public class CrmSalerCustomerController extends BaseController
{
    @Autowired
    private ICrmSalerCustomerService crmSalerCustomerService;

    /**
     * 查询销售人员与客户中间列表
     */
    @PreAuthorize("@ss.hasPermi('salercustomer:salercustomer:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmSalerCustomer crmSalerCustomer)
    {
        startPage();
        List<CrmSalerCustomer> list = crmSalerCustomerService.selectCrmSalerCustomerList(crmSalerCustomer);
        return getDataTable(list);
    }

    /**
     * 导出销售人员与客户中间列表
     */
    @PreAuthorize("@ss.hasPermi('salercustomer:salercustomer:export')")
    @Log(title = "销售人员与客户中间", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CrmSalerCustomer crmSalerCustomer)
    {
        List<CrmSalerCustomer> list = crmSalerCustomerService.selectCrmSalerCustomerList(crmSalerCustomer);
        ExcelUtil<CrmSalerCustomer> util = new ExcelUtil<CrmSalerCustomer>(CrmSalerCustomer.class);
        util.exportExcel(response, list, "销售人员与客户中间数据");
    }

    /**
     * 获取销售人员与客户中间详细信息
     */
    @PreAuthorize("@ss.hasPermi('salercustomer:salercustomer:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(crmSalerCustomerService.selectCrmSalerCustomerById(id));
    }

    /**
     * 新增销售人员与客户中间
     */
    @PreAuthorize("@ss.hasPermi('salercustomer:salercustomer:add')")
    @Log(title = "销售人员与客户中间", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmSalerCustomer crmSalerCustomer)
    {
        return toAjax(crmSalerCustomerService.insertCrmSalerCustomer(crmSalerCustomer));
    }

    /**
     * 修改销售人员与客户中间
     */
    @PreAuthorize("@ss.hasPermi('salercustomer:salercustomer:edit')")
    @Log(title = "销售人员与客户中间", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmSalerCustomer crmSalerCustomer)
    {
        return toAjax(crmSalerCustomerService.updateCrmSalerCustomer(crmSalerCustomer));
    }

    /**
     * 删除销售人员与客户中间
     */
    @PreAuthorize("@ss.hasPermi('salercustomer:salercustomer:remove')")
    @Log(title = "销售人员与客户中间", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(crmSalerCustomerService.deleteCrmSalerCustomerByIds(ids));
    }
}

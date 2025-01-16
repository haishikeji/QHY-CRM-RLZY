package cn.px.crm.approval.controller;

import java.util.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import cn.px.common.core.domain.entity.SysUser;
import cn.px.common.utils.SecurityUtils;
import cn.px.crm.customer.domain.CrmCustomer;
import cn.px.crm.customer.service.ICrmCustomerService;
import cn.px.crm.translog.domain.CrmTranslog;
import cn.px.crm.translog.service.ICrmTranslogService;
import cn.px.system.service.ISysPostService;
import cn.px.system.service.ISysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
import cn.px.crm.approval.domain.CrmApproval;
import cn.px.crm.approval.service.ICrmApprovalService;
import cn.px.common.utils.poi.ExcelUtil;
import cn.px.common.core.page.TableDataInfo;

/**
 * 审批业务Controller
 *
 * @author 品讯科技
 * @date 2024-08
 */
@RestController
@RequestMapping("/approval/approval")
public class CrmApprovalController extends BaseController {
    @Autowired
    private ICrmApprovalService crmApprovalService;
    @Resource
    private ISysUserService userService;
    @Autowired
    private ICrmCustomerService crmCustomerService;
    @Resource
    private ICrmTranslogService translogService;
    @Resource
    private ISysPostService postService;
    /**
     * 查询审批业务列表
     */
    @PreAuthorize("@ss.hasPermi('approval:approval:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmApproval crmApproval) {
        if (!"admin".equals(getUsername())) {
            crmApproval.setUid(SecurityUtils.getUserId());
        }
        startPage();
        List<CrmApproval> list = crmApprovalService.selectCrmApprovalList(crmApproval);
        if (list.size() > 0) {
            for (CrmApproval approval : list) {
                String copyId = approval.getCopyId();
                String[] ids = copyId.split(",");
                List<String> userNameList = new ArrayList<>();
                for (String id : ids) {
                    SysUser sysUser = userService.selectUserById(Long.valueOf(id));
                    userNameList.add(sysUser.getUserName());
                }
                String join = String.join(",", userNameList);
                approval.setCopyId(join);
            }
        }
        return getDataTable(list);
    }

    /**
     * 查询我的申请
     */
    @PreAuthorize("@ss.hasPermi('approval:approval:list')")
    @GetMapping("/myApplyList")
    public TableDataInfo myApplyList(CrmApproval crmApproval) {
        crmApproval.setSponsorId(getUserId());
        startPage();
        List<CrmApproval> list = crmApprovalService.selectCrmApprovalList(crmApproval);
        if (list.size() > 0) {
            for (CrmApproval approval : list) {
                String copyId = approval.getCopyId();
                String[] ids = copyId.split(",");
                List<String> userNameList = new ArrayList<>();
                for (String id : ids) {
                    SysUser sysUser = userService.selectUserById(Long.valueOf(id));
                    userNameList.add(sysUser.getUserName());
                }
                String join = String.join(",", userNameList);
                approval.setCopyId(join);
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出审批业务列表
     */
    @PreAuthorize("@ss.hasPermi('approval:approval:export')")
    @Log(title = "审批业务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CrmApproval crmApproval) {
        List<CrmApproval> list = crmApprovalService.selectCrmApprovalList(crmApproval);
        ExcelUtil<CrmApproval> util = new ExcelUtil<CrmApproval>(CrmApproval.class);
        util.exportExcel(response, list, "审批业务数据");
    }

    /**
     * 获取审批业务详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(crmApprovalService.selectCrmApprovalById(id));
    }

    /**
     * 新增审批业务
     */
    @PreAuthorize("@ss.hasPermi('approval:approval:add')")
    @Log(title = "审批业务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmApproval crmApproval) {
        return toAjax(crmApprovalService.insertCrmApproval(crmApproval));
    }

    /**
     * 修改审批业务
     */
    @Log(title = "审批业务", businessType = BusinessType.UPDATE)
    @PutMapping
    @Transactional
    public AjaxResult edit(@RequestBody CrmApproval crmApproval) {
        CrmApproval approval = crmApprovalService.selectCrmApprovalById(crmApproval.getId());
        if ("1".equals(crmApproval.getStatus())) {
            //通过
            //更新时间
            String defer = approval.getDefer();
            //修改客户延期时间
            CrmCustomer customer = crmCustomerService.selectCrmCustomerById(approval.getCid());
            //设置到期时间
            Date date = customer.getUpdateTime();//取时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, Integer.parseInt(defer));
            Date time = calendar.getTime();
            customer.setUpdateTime(time);
            customer.setStatus("1");
            crmCustomerService.updateCrmCustomer(customer);
            //添加操作记录
            CrmTranslog crmTranslog = new CrmTranslog();
            crmTranslog.setCid(customer.getId());
            crmTranslog.setUid(SecurityUtils.getUserId());
            crmTranslog.setContent("拒绝审核，客户回到公海");
            translogService.insertCrmTranslog(crmTranslog);
        }
        return toAjax(crmApprovalService.updateCrmApproval(crmApproval));
    }

    /**
     * 删除审批业务
     */
    @PreAuthorize("@ss.hasPermi('approval:approval:remove')")
    @Log(title = "审批业务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(crmApprovalService.deleteCrmApprovalByIds(ids));
    }
}

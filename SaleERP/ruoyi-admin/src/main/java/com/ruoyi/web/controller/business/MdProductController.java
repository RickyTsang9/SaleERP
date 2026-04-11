package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.service.IMdProductService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/product")
public class MdProductController extends BaseController
{
    @Autowired
    private IMdProductService mdProductService;

    @PreAuthorize("@ss.hasPermi('business:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdProduct mdProduct)
    {
        startPage();
        List<MdProduct> mdProductList = mdProductService.selectMdProductList(mdProduct);
        return getDataTable(mdProductList);
    }

    @PreAuthorize("@ss.hasPermi('business:product:export')")
    @Log(title = "商品资料", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, MdProduct mdProduct)
    {
        List<MdProduct> mdProductList = mdProductService.selectMdProductList(mdProduct);
        ExcelUtil<MdProduct> util = new ExcelUtil<MdProduct>(MdProduct.class);
        util.exportExcel(response, mdProductList, "商品资料数据");
    }

    @PreAuthorize("@ss.hasPermi('business:product:import')")
    @Log(title = "商品资料", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<MdProduct> util = new ExcelUtil<MdProduct>(MdProduct.class);
        List<MdProduct> productList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = mdProductService.importMdProduct(productList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(javax.servlet.http.HttpServletResponse response)
    {
        ExcelUtil<MdProduct> util = new ExcelUtil<MdProduct>(MdProduct.class);
        util.importTemplateExcel(response, "商品资料数据");
    }

    @PreAuthorize("@ss.hasPermi('business:product:query')")
    @GetMapping("/{productId}")
    public AjaxResult getInfo(@PathVariable Long productId)
    {
        return success(mdProductService.selectMdProductById(productId));
    }

    @PreAuthorize("@ss.hasPermi('business:product:add')")
    @Log(title = "商品资料", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody MdProduct mdProduct)
    {
        mdProduct.setCreateBy(getUsername());
        return toAjax(mdProductService.insertMdProduct(mdProduct));
    }

    @PreAuthorize("@ss.hasPermi('business:product:edit')")
    @Log(title = "商品资料", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody MdProduct mdProduct)
    {
        mdProduct.setUpdateBy(getUsername());
        return toAjax(mdProductService.updateMdProduct(mdProduct));
    }

    @PreAuthorize("@ss.hasPermi('business:product:edit')")
    @Log(title = "商品资料", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody MdProduct mdProduct)
    {
        mdProduct.setUpdateBy(getUsername());
        return toAjax(mdProductService.updateMdProduct(mdProduct));
    }

    @PreAuthorize("@ss.hasPermi('business:product:remove')")
    @Log(title = "商品资料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds)
    {
        return toAjax(mdProductService.deleteMdProductByIds(productIds));
    }
}

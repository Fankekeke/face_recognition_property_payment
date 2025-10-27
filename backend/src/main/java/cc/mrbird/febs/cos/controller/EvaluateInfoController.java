package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.EvaluateInfo;
import cc.mrbird.febs.cos.service.IEvaluateInfoService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/evaluate-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EvaluateInfoController {

    private final IEvaluateInfoService evaluateInfoService;

    /**
     * 分页获取评价信息
     *
     * @param page
     * @param evaluateInfo
     * @return
     */
    @GetMapping("/page")
    public R page(Page<EvaluateInfo> page, EvaluateInfo evaluateInfo) {
        return R.ok(evaluateInfoService.selectEvaluatePage(page, evaluateInfo));
    }

    /**
     * 导入信息列表
     */
    @PostMapping("/import")
    public R importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<EvaluateInfo> result = evaluateInfoService.importExcel(file);
        if (CollectionUtil.isEmpty(result)) {
            throw new FebsException("请检查是否正确");
        }
        return R.ok(result);
    }

    /**
     * 添加评价信息
     *
     * @param evaluateInfo
     * @return
     */
    @PostMapping
    public R save(EvaluateInfo evaluateInfo) throws FebsException {
        if (StrUtil.isEmpty(evaluateInfo.getEvaluateInfoList())) {
            throw new FebsException("请填写评价信息");
        }
        List<EvaluateInfo> reportList = JSONUtil.toList(evaluateInfo.getEvaluateInfoList(), EvaluateInfo.class);
        reportList.forEach(report -> {
            report.setEvaluateUser(evaluateInfo.getEvaluateUser());
            report.setCreateDate(DateUtil.formatDateTime(new Date()));
        });
        return R.ok(evaluateInfoService.saveBatch(reportList));
    }

    /**
     * 修改评价信息
     *
     * @param evaluateInfo
     * @return
     */
    @PutMapping
    public R edit(EvaluateInfo evaluateInfo) {
        return R.ok(evaluateInfoService.updateById(evaluateInfo));
    }

    /**
     * 删除评价信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(evaluateInfoService.removeByIds(ids));
    }
}

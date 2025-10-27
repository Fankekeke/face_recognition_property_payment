package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.cos.entity.EvaluateInfo;
import cc.mrbird.febs.cos.dao.EvaluateInfoMapper;
import cc.mrbird.febs.cos.entity.WorkerInfo;
import cc.mrbird.febs.cos.service.IEvaluateInfoService;
import cc.mrbird.febs.cos.service.IWorkerInfoService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EvaluateInfoServiceImpl extends ServiceImpl<EvaluateInfoMapper, EvaluateInfo> implements IEvaluateInfoService {

    private final IWorkerInfoService workerInfoService;

    private static final BigDecimal QUALITY_WEIGHT = BigDecimal.valueOf(0.3);
    private static final BigDecimal EFFICIENCY_WEIGHT = BigDecimal.valueOf(0.35);
    private static final BigDecimal SATISFACTION_WEIGHT = BigDecimal.valueOf(0.2);
    private static final BigDecimal TEAM_WEIGHT = BigDecimal.valueOf(0.15);

    /**
     * 查询评价信息
     *
     * @param page         分页对象
     * @param evaluateInfo 评价信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> selectEvaluatePage(IPage<EvaluateInfo> page, EvaluateInfo evaluateInfo) {
        return baseMapper.selectEvaluatePage(page, evaluateInfo);
    }

    /**
     * 导入信息列表
     *
     * @param file 文件
     * @return 结果
     * @throws Exception 异常
     */
    @Override
    public List<EvaluateInfo> importExcel(MultipartFile file) throws Exception {
        ExcelReader excelReader = ExcelUtil.getReader(file.getInputStream(), 0);
        setExcelHeaderAlias(excelReader);
        List<EvaluateInfo> reportList = excelReader.read(1, 2, Integer.MAX_VALUE, EvaluateInfo.class);

        // 员工信息
        List<WorkerInfo> workerInfoList = workerInfoService.list();
        Map<String, WorkerInfo> staffMap = workerInfoList.stream().collect(Collectors.toMap(WorkerInfo::getName, e -> e));

        if (CollectionUtil.isEmpty(reportList)) {
            throw new FebsException("导入数据不得为空");
        }

        // 判断评价周期是否已经存在
        Integer year = reportList.get(0).getYear();
        Integer quarterly = reportList.get(0).getQuarterly();
        if (this.count(Wrappers.<EvaluateInfo>lambdaQuery().eq(EvaluateInfo::getYear, year).eq(EvaluateInfo::getQuarterly, quarterly)) > 0) {
            throw new FebsException("该评价周期已存在");
        }

        // 导入数据
        List<EvaluateInfo> reports = new ArrayList<>();
        for (EvaluateInfo report : reportList) {
            if (staffMap.get(report.getStaffName()) != null) {
                reports.add(report);
            }
            WorkerInfo workerInfo = staffMap.get(report.getStaffName());
            report.setStaffId(workerInfo.getId());
            // 计算总分 服务质量	工作效率	客户满意度	团队协作与技能培训
            //百分制30%	百分制 35%	百分制20%	百分制15%
            BigDecimal qualityScore = report.getQualityScore() != null ? report.getQualityScore() : BigDecimal.ZERO;
            BigDecimal efficiencyScore = report.getEfficiencyScore() != null ? report.getEfficiencyScore() : BigDecimal.ZERO;
            BigDecimal satisfactionScore = report.getSatisfactionScore() != null ? report.getSatisfactionScore() : BigDecimal.ZERO;
            BigDecimal teamScore = report.getTeamScore() != null ? report.getTeamScore() : BigDecimal.ZERO;

            BigDecimal totalScore = qualityScore.multiply(QUALITY_WEIGHT)
                    .add(efficiencyScore.multiply(EFFICIENCY_WEIGHT))
                    .add(satisfactionScore.multiply(SATISFACTION_WEIGHT))
                    .add(teamScore.multiply(TEAM_WEIGHT));

            report.setTotalScore(totalScore);
        }
        return reports;
    }

    /**
     * 设置HeaderAlias
     *
     * @param excelReader HeaderAlias
     */
    public void setExcelHeaderAlias(ExcelReader excelReader) {
        excelReader.addHeaderAlias("被评人", "staffName");
        excelReader.addHeaderAlias("评价年份", "year");
        excelReader.addHeaderAlias("评价季度", "quarterly");
        excelReader.addHeaderAlias("服务质量", "qualityScore");
        excelReader.addHeaderAlias("工作效率", "efficiencyScore");
        excelReader.addHeaderAlias("客户满意度", "satisfactionScore");
        excelReader.addHeaderAlias("团队协作与技能培训", "teamScore");
    }
}

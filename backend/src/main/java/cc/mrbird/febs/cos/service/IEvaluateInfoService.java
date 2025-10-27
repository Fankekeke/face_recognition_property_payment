package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.EvaluateInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
public interface IEvaluateInfoService extends IService<EvaluateInfo> {

    /**
     * 查询评价信息
     *
     * @param page         分页对象
     * @param evaluateInfo 评价信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> selectEvaluatePage(IPage<EvaluateInfo> page, EvaluateInfo evaluateInfo);

    /**
     * 导入信息列表
     *
     * @param file 文件
     * @return 结果
     */
    List<EvaluateInfo> importExcel(MultipartFile file) throws Exception;
}

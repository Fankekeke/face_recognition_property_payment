package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.PaymentManage;
import cc.mrbird.febs.cos.dao.PaymentManageMapper;
import cc.mrbird.febs.cos.entity.PaymentRecord;
import cc.mrbird.febs.cos.service.IPaymentManageService;
import cc.mrbird.febs.cos.service.IPaymentRecordService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentManageServiceImpl extends ServiceImpl<PaymentManageMapper, PaymentManage> implements IPaymentManageService {

    private final IPaymentRecordService paymentRecordService;

    @Override
    public IPage<LinkedHashMap<String, Object>> paymentManageByPage(Page page, PaymentManage paymentManage) {
        return baseMapper.paymentManageByPage(page, paymentManage);
    }

    @Override
    public LinkedHashMap<String, Object> selectHomeDataByAdmin() {
        // 返回数据
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

        // 本月缴费数量
        Integer year = DateUtil.year(new Date());
        Integer month = DateUtil.month(new Date()) + 1;

        List<PaymentManage> paymentManageMonth = this.list(Wrappers.<PaymentManage>lambdaQuery().eq(PaymentManage::getYear, year).eq(PaymentManage::getMonth, month));
        if (CollectionUtil.isEmpty(paymentManageMonth)) {
            result.put("monthPaymentNum", 0);
            result.put("monthPayable", 0);
            result.put("monthPaid", 0);
            result.put("monthUnpaid", 0);
        } else {
            Map<Integer, BigDecimal> map = paymentManageMonth.stream().collect(Collectors.toMap(PaymentManage::getId, PaymentManage::getPrice));
            result.put("monthPaymentNum", paymentManageMonth.size());
            // 本月应缴费用
            BigDecimal monthPayable = paymentManageMonth.stream().map(PaymentManage::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            result.put("monthPayable", monthPayable);
            // 获取已交费用
            List<Integer> paymentIds = paymentManageMonth.stream().map(PaymentManage::getId).collect(Collectors.toList());
            List<PaymentRecord> paymentRecordList = paymentRecordService.list(Wrappers.<PaymentRecord>lambdaQuery().in(PaymentRecord::getPaymentId, paymentIds));
            // 本月已缴费用
            if (CollectionUtil.isEmpty(paymentRecordList)) {
                result.put("monthPaid", 0);
                result.put("monthUnpaid", monthPayable);
            } else {
                BigDecimal paid = BigDecimal.ZERO;
                for (PaymentRecord paymentRecord : paymentRecordList) {
                    paid = paid.add(map.get(paymentRecord.getPaymentId()));
                }
                result.put("monthPaid", paid);
                result.put("monthUnpaid", monthPayable.subtract(paid));
            }
        }

        // 近十天缴费统计
        result.put("orderNumDayList", baseMapper.selectOrderNumWithinDays());
        // 近十天报修统计
        result.put("priceDayList", baseMapper.selectOrderPriceWithinDays());
        // 缴费类型统计
        result.put("orderType", baseMapper.selectOrderDishesType(year, month));
        return result;
    }

    /**
     * 根据房屋ID查询费用信息
     *
     * @param houseId 房产ID
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> queryPaymentByHouseId(Integer houseId) {
        // 返回数据
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>() {
            {
                put("data1", Collections.emptyList());
                put("data2", Collections.emptyList());
                put("data3", Collections.emptyList());
                put("data4", Collections.emptyList());
            }
        };
        List<Integer> typeList = new ArrayList<>();
        typeList.add(1);
        typeList.add(2);
        // 查询费用信息
        List<PaymentManage> paymentManageList = this.list(Wrappers.<PaymentManage>lambdaQuery().eq(PaymentManage::getHousesId, houseId).eq(PaymentManage::getYear, DateUtil.year(new Date())).in(PaymentManage::getType, typeList));
        if (CollectionUtil.isEmpty(paymentManageList)) {
            return result;
        }

        Map<String, List<PaymentManage>> paymentRecordTypeMap = paymentManageList.stream().collect(Collectors.groupingBy(e -> e.getMonth() + "-" + e.getType()));
        Map<Integer, List<PaymentManage>> paymentRecordPriceMap = paymentManageList.stream().collect(Collectors.groupingBy(PaymentManage::getMonth));

        //  获取缴费记录
        List<Integer> paymentIds = paymentManageList.stream().map(PaymentManage::getId).collect(Collectors.toList());
        List<PaymentRecord> paymentRecordList = paymentRecordService.list(Wrappers.<PaymentRecord>lambdaQuery().in(PaymentRecord::getPaymentId, paymentIds));
        Map<Integer, PaymentRecord> paymentRecordMap = paymentRecordList.stream().collect(Collectors.toMap(PaymentRecord::getPaymentId, paymentRecord -> paymentRecord));

        List<LinkedHashMap<String, Object>> data1 = new ArrayList<>();
        List<LinkedHashMap<String, Object>> data2 = new ArrayList<>();
        List<LinkedHashMap<String, Object>> data3 = new ArrayList<>();
        List<LinkedHashMap<String, Object>> data4 = new ArrayList<>();

        // 计算每个月
        for (int i = 1; i < 13; i++) {
            LinkedHashMap<String, Object> item1 = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, Object> item2 = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, Object> item3 = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, Object> item4 = new LinkedHashMap<String, Object>();
            item1.put("name", i + "月");
            item2.put("name", i + "月");
            item3.put("name", i + "月");
            item4.put("name", i + "月");
            // 获取此月用电量

            if (paymentRecordTypeMap.containsKey(i + "-1")) {
                List<PaymentManage> paymentManage = paymentRecordTypeMap.get(i + "-1");
                item1.put("value", paymentManage.stream().map(PaymentManage::getDosage).reduce(BigDecimal.ZERO, BigDecimal::add));
            } else {
                item1.put("value", 0);
            }

            if (paymentRecordTypeMap.containsKey(i + "-2")) {
                List<PaymentManage> paymentManage = paymentRecordTypeMap.get(i + "-2");
                item2.put("value", paymentManage.stream().map(PaymentManage::getDosage).reduce(BigDecimal.ZERO, BigDecimal::add));
            } else {
                item2.put("value", 0);
            }

            // 本月应缴费用
            if (paymentRecordPriceMap.containsKey(i)) {
                List<PaymentManage> paymentManage = paymentRecordPriceMap.get(i);
                item3.put("value", paymentManage.stream().map(PaymentManage::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
            } else {
                item3.put("value", 0);
            }

            // 本月实缴费用
            if (paymentRecordPriceMap.containsKey(i)) {
                BigDecimal monthPayable = BigDecimal.ZERO;
                List<PaymentManage> paymentManage = paymentRecordPriceMap.get(i);
                for (PaymentManage manage : paymentManage) {
                    if (paymentRecordMap.get(manage.getId()) != null) {
                        monthPayable = monthPayable.add(manage.getPrice());
                    }
                }
                item4.put("value", monthPayable);
            } else {
                item4.put("value", 0);
            }

            data1.add(item1);
            data2.add(item2);
            data3.add(item3);
            data4.add(item4);
        }

        result.put("data1", data1);
        result.put("data2", data2);
        result.put("data3", data3);
        result.put("data4", data4);

        return result;
    }
}

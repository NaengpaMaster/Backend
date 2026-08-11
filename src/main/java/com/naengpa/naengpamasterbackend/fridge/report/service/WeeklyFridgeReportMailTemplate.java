package com.naengpa.naengpamasterbackend.fridge.report.service;

import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyConsumedCategorySummary;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyConsumedProductSummary;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyFridgeReportSummary;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyRemainingFridgeItemSummary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class WeeklyFridgeReportMailTemplate {

    public String subject(List<WeeklyFridgeReportSummary> summaries) {
        if (summaries.size() == 1) {
            return "[냉파마스터] %s 주간 냉장고 리포트".formatted(summaries.getFirst().fridgeName());
        }
        return "[냉파마스터] 주간 냉장고 통합 리포트";
    }

    public String html(List<WeeklyFridgeReportSummary> summaries) {
        long totalCount = summaries.stream()
                .mapToLong(WeeklyFridgeReportSummary::totalConsumedCount)
                .sum();
        String period = summaries.isEmpty()
                ? ""
                : "%s ~ %s".formatted(summaries.getFirst().startDate(), summaries.getFirst().endDate());

        StringBuilder builder = new StringBuilder();
        builder.append("""
                <!doctype html>
                <html>
                <body style="margin:0;background:#f5f7f4;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;color:#24312d;">
                  <div style="max-width:720px;margin:0 auto;padding:28px 18px;">
                    <div style="background:#1f6f54;border-radius:24px;padding:26px;color:#fff;">
                      <div style="font-size:13px;font-weight:800;opacity:.8;letter-spacing:.08em;">REFRIDGE WEEKLY REPORT</div>
                      <div style="font-size:28px;font-weight:900;margin-top:8px;">이번 주 냉파 리포트</div>
                      <div style="font-size:14px;opacity:.86;margin-top:8px;">%s</div>
                    </div>
                    <div style="display:grid;grid-template-columns:repeat(1,minmax(0,1fr));gap:12px;margin-top:16px;">
                      %s
                    </div>
                    <div style="background:#fff;border:1px solid #dde6df;border-radius:20px;padding:20px;margin-top:14px;">
                      <div style="font-size:18px;font-weight:900;">이번 주 가장 많이 소비된 재료 카테고리</div>
                      <div style="margin-top:16px;">%s</div>
                    </div>
                """.formatted(
                escape(period),
                metricCard("이번 주 소비 기록", totalCount + "건", "전부 사용 · 일부 사용 · 나눔으로 기록된 식재료 기준입니다."),
                categoryGraph(summaries)
        ));

        for (WeeklyFridgeReportSummary summary : summaries) {
            builder.append(fridgeSection(summary));
        }

        builder.append("""
                    <div style="background:linear-gradient(135deg,#fff7ed,#ecfdf5);border:1px solid #fed7aa;border-radius:24px;padding:22px;margin-top:14px;box-shadow:0 10px 28px rgba(31,111,84,.12);">
                      <div style="display:inline-block;background:#ffedd5;color:#9a3412;border-radius:999px;padding:6px 10px;font-size:12px;font-weight:900;">냉파마스터 PLUS</div>
                      <div style="font-size:20px;font-weight:900;color:#1f6f54;margin-top:10px;">가족 냉장고까지 한눈에 관리해보세요</div>
                      <div style="font-size:14px;line-height:1.75;color:#5f3b16;margin-top:8px;">
                        구독하면 가족 냉장고 소비 흐름, 식재료 요청/나눔 기록, 주간 냉파 리포트를 더 편하게 확인할 수 있습니다.
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """);

        return builder.toString();
    }

    public String body(WeeklyFridgeReportSummary summary) {
        StringBuilder builder = new StringBuilder();
        builder.append("냉파마스터 주간 냉장고 리포트입니다.\n\n");
        builder.append("냉장고: ").append(summary.fridgeName()).append("\n");
        builder.append("기간: ").append(summary.startDate()).append(" ~ ").append(summary.endDate()).append("\n\n");

        if (!summary.hasConsumption()) {
            builder.append("이번 주에는 사용 처리된 식재료가 없습니다.\n");
            builder.append("냉장고를 정리하고 사용한 재료를 기록하면 다음 리포트에서 소비 흐름을 확인할 수 있어요.\n");
            return builder.toString();
        }

        builder.append("총 소비 건수: ").append(summary.totalConsumedCount()).append("건\n\n");
        builder.append("많이 사용한 재료 TOP\n");
        int productRank = 1;
        for (WeeklyConsumedProductSummary product : summary.topProducts()) {
            builder.append(productRank++)
                    .append(". ")
                    .append(product.productName())
                    .append(" - ")
                    .append(product.count())
                    .append("건\n");
        }

        builder.append("\n카테고리 요약\n");
        for (WeeklyConsumedCategorySummary category : summary.categories()) {
            builder.append("- ")
                    .append(category.categoryName())
                    .append(": ")
                    .append(category.count())
                    .append("건, ")
                    .append(category.ratio())
                    .append("%\n");
        }

        builder.append("\n냉파 코멘트\n");
        builder.append("이번 주 소비 기록을 기준으로 자주 쓰는 재료를 먼저 보충하고, 덜 쓰는 재료는 다음 식단에서 우선 사용해보세요.\n");
        return builder.toString();
    }

    private String fridgeSection(WeeklyFridgeReportSummary summary) {
        String lessConsumed = summary.categories()
                .stream()
                .min(Comparator.comparingLong(WeeklyConsumedCategorySummary::count))
                .map(category -> "%s 소비가 상대적으로 적었습니다.".formatted(category.categoryName()))
                .orElse("이번 주 사용 처리된 식재료가 아직 없습니다.");

        String title = summary.fridgeName().contains("가족")
                ? "가족냉장고 중 이 부분 소비가 덜됐습니다"
                : "내 냉장고 중 이 부분 소비를 하고, 이 부분은 아직 소비되지 않았습니다";

        return """
                <div style="background:#fff;border:1px solid #dde6df;border-radius:20px;padding:20px;margin-top:14px;">
                  <div style="font-size:12px;font-weight:900;color:#1f6f54;">%s</div>
                  <div style="font-size:20px;font-weight:900;margin-top:4px;">%s</div>
                  <div style="font-size:13px;color:#66746d;margin-top:6px;">%s</div>
                  <div style="font-size:15px;font-weight:900;margin-top:16px;">이번 주 사용한 식재료</div>
                  <div style="margin-top:10px;">%s</div>
                  <div style="font-size:15px;font-weight:900;margin-top:18px;">아직 냉장고에 남은 식재료</div>
                  <div style="margin-top:10px;">%s</div>
                </div>
                """.formatted(
                escape(title),
                escape(summary.fridgeName()),
                escape(lessConsumed),
                categoryGraph(List.of(summary)),
                remainingItems(summary.remainingItems())
        );
    }

    private String remainingItems(List<WeeklyRemainingFridgeItemSummary> remainingItems) {
        if (remainingItems.isEmpty()) {
            return """
                    <div style="padding:14px;border-radius:14px;background:#f5f7f4;color:#66746d;font-size:13px;">
                      현재 냉장고에 남은 식재료가 없습니다.
                    </div>
                    """;
        }

        StringBuilder builder = new StringBuilder();
        for (WeeklyRemainingFridgeItemSummary item : remainingItems) {
            builder.append("""
                    <div style="display:flex;justify-content:space-between;gap:12px;padding:12px 0;border-bottom:1px solid #eef3ef;">
                      <div>
                        <div style="font-size:14px;font-weight:900;color:#24312d;">%s</div>
                        <div style="font-size:12px;color:#66746d;margin-top:3px;">수량 %s</div>
                      </div>
                      <div style="font-size:12px;color:#1f6f54;font-weight:800;white-space:nowrap;">%s</div>
                    </div>
                    """.formatted(
                    escape(item.productName()),
                    escape(item.quantity()),
                    item.expiryDate() == null ? "기한 없음" : escape(item.expiryDate().toString())
            ));
        }
        return builder.toString();
    }

    private String categoryGraph(List<WeeklyFridgeReportSummary> summaries) {
        List<WeeklyConsumedCategorySummary> categories = summaries.stream()
                .flatMap(summary -> summary.categories().stream())
                .toList();

        if (categories.isEmpty()) {
            return """
                    <div style="padding:14px;border-radius:14px;background:#f5f7f4;color:#66746d;font-size:13px;">
                      이번 주 사용 처리된 식재료가 아직 없습니다.
                    </div>
                    """;
        }

        long maxCount = categories.stream()
                .mapToLong(WeeklyConsumedCategorySummary::count)
                .max()
                .orElse(1L);

        StringBuilder builder = new StringBuilder();
        for (WeeklyConsumedCategorySummary category : categories) {
            long width = Math.max(8, Math.round((category.count() * 100.0) / maxCount));
            builder.append("""
                    <div style="margin-bottom:12px;">
                      <div style="display:flex;justify-content:space-between;font-size:13px;font-weight:800;margin-bottom:6px;">
                        <span>%s</span><span>%d건</span>
                      </div>
                      <div style="height:12px;background:#edf3ef;border-radius:999px;overflow:hidden;">
                        <div style="height:12px;width:%d%%;background:linear-gradient(90deg,#1f6f54,#72b89a);border-radius:999px;"></div>
                      </div>
                    </div>
                    """.formatted(escape(category.categoryName()), category.count(), width));
        }
        return builder.toString();
    }

    private String metricCard(String label, String value, String description) {
        return """
                <div style="background:#fff;border:1px solid #dde6df;border-radius:18px;padding:16px;">
                  <div style="font-size:12px;font-weight:900;color:#66746d;">%s</div>
                  <div style="font-size:24px;font-weight:900;color:#1f6f54;margin-top:6px;">%s</div>
                  <div style="font-size:12px;color:#66746d;line-height:1.5;margin-top:6px;">%s</div>
                </div>
                """.formatted(escape(label), escape(value), escape(description));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

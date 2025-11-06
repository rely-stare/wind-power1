package com.tc.common.enums;

import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 告警规则判断
 * @author sunchao
 * @date 2024/3/18 13:43
 */
public enum ExpressionEnum {
    EQ("==") {
        @Override
        boolean check(BigDecimal source, BigDecimal... targets) {
            return source.compareTo(targets[0]) == 0;
        }
    },
    NEQ("!=") {
        @Override
        boolean check(BigDecimal source, BigDecimal... targets) {
            return source.compareTo(targets[0]) != 0;
        }
    },
    GT(">") {
        @Override
        boolean check(BigDecimal source, BigDecimal... targets) {
            return source.compareTo(targets[0]) > 0;
        }
    },
    LT("<") {
        @Override
        boolean check(BigDecimal source, BigDecimal... targets) {
            return source.compareTo(targets[0]) < 0;
        }
    },
    GTE(">=") {
        @Override
        boolean check(BigDecimal source, BigDecimal... targets) {
            return source.compareTo(targets[0]) >= 0;
        }
    },
    LTE("<=") {
        @Override
        boolean check(BigDecimal source, BigDecimal... targets) {
            return source.compareTo(targets[0]) <= 0;
        }
    },
    IN("in") {
        @Override
        boolean check(BigDecimal source, BigDecimal... targets) {
            for (BigDecimal target : targets) {
                boolean res = EQ.check(source, target);
                if (res) {
                    return true;
                }
            }
            return false;
        }
    },
    BETWEEN("between") {
        @Override
        boolean check(BigDecimal source, BigDecimal... targets) {
            Assert.isTrue(targets.length > 1, "目标对象个数有误");
            return GTE.check(source, targets[0]) && LTE.check(source, targets[1]);
        }
    },
    ;

    private final String expression;

    ExpressionEnum(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    abstract boolean check(BigDecimal source, BigDecimal... targets);

    /**
     * 检查是否正确
     *
     * @param expression 表达式：>、>=、<、<=、==、!=、in、between
     * @param source     被比较的数字字符
     * @param target     指标数字字符，多个用英文逗号隔开（in,between条件下支持多个）
     * @return
     */
    public static boolean check(String expression, String source, String target) {
        ExpressionEnum[] expressionEnums = ExpressionEnum.values();
        ExpressionEnum expressionEnum = null;
        for (ExpressionEnum item : expressionEnums) {
            boolean equals = item.getExpression().equals(expression);
            if (equals) {
                expressionEnum = item;
                break;
            }
        }
        Assert.notNull(expressionEnum, "未找到表达式");
        Assert.notNull(target, "目标对象不能为空");
        BigDecimal sourceDecimal = BigDecimal.valueOf(Double.parseDouble(source));
        // 多个值需要先转为数组再比较
        String[] split = target.split(",");
        BigDecimal[] bigDecimals = Arrays.stream(split)
                .map(t -> BigDecimal.valueOf(Double.parseDouble(t)))
                .toArray(BigDecimal[]::new);
        return expressionEnum.check(sourceDecimal, bigDecimals);
    }

    /**
     * 主函数：用于测试
     *
     * @param args
     */
    public static void main(String[] args) {
        String source = "3.15";
        ExpressionEnum[] values = values();
        for (ExpressionEnum value : values) {
            if (value.equals(ExpressionEnum.BETWEEN)) {
                continue;
            }
            test(value, source, "3.15");
            test(value, source, "3");
            test(value, source, "4");
        }
        test(ExpressionEnum.BETWEEN, source, "3.13,3.16");
        test(ExpressionEnum.BETWEEN, source, "4,5");
    }

    private static void test(ExpressionEnum expressionEnum, String source, String target) {
        String expression = expressionEnum.getExpression();
        String res = source + " " + expression + " " + target + " : " + check(expression, source, target);
        System.out.println(res);
    }
}

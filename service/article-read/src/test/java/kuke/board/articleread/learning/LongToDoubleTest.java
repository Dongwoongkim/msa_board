package kuke.board.articleread.learning;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class LongToDoubleTest {

    @Test
    void longToDoubleTest() {
        // long -> 64bit로 정수
        long longValue = 111_111_111_111_111_111L;
        System.out.println("longValue: " + longValue);

        // double -> 64bit로 부동소수점
        double doubleValue = longValue;
        System.out.println("doubleValue: " + new BigDecimal(doubleValue).toString());
        long longValue2 = (long) doubleValue;
        System.out.println("longValue2: " + longValue2);
    }

}

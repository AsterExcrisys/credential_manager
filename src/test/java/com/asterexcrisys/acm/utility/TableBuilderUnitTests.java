package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.services.console.TableBuilder;
import com.asterexcrisys.acm.types.console.CellSize;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TableBuilderUnitTests {

    private TableBuilder builder = new TableBuilder();

    @BeforeEach
    public void setUp() {
        builder = new TableBuilder(CellSize.WRAP_SMALL);
    }

    @AfterEach
    public void tearDown() {
        builder.close();
    }

    @Test
    public void shouldBuildTable() {
        builder.addAttribute("A").addAttribute("B").addAttribute("C");
        builder.addRecord(List.of("X", "Y", "Z")).addRecord(List.of("X", "Y", "Z"));
        assertEquals(
                """
                        +-------------------------+-------------------------+-------------------------+
                        | A                       | B                       | C                       |\s
                        +-------------------------+-------------------------+-------------------------+
                        +-------------------------+-------------------------+-------------------------+
                        | X                       | Y                       | Z                       |\s
                        +-------------------------+-------------------------+-------------------------+
                        | X                       | Y                       | Z                       |\s
                        +-------------------------+-------------------------+-------------------------+
                        """,
                builder.build()
        );
    }

    @Test
    public void shouldBuildTableMultiLine() {
        builder.addAttribute("AAAAAAAAAAAAAAAAAAAAAAAAAA").addAttribute("BBBBBBBBBBBBBBBBBBBBBBBB").addAttribute("CCCCCCCCCCCCCCCCC");
        builder.addRecord(List.of("X", "Y", "Z")).addRecord(List.of("X", "Y", "Z"));
        assertEquals(
                """
                        +-------------------------+-------------------------+-------------------------+
                        | AAAAAAAAAAAAAAAAAAAAAAA | BBBBBBBBBBBBBBBBBBBBBBB | CCCCCCCCCCCCCCCCC       |\s
                        | AAA                     | B                       |                         |\s
                        +-------------------------+-------------------------+-------------------------+
                        +-------------------------+-------------------------+-------------------------+
                        | X                       | Y                       | Z                       |\s
                        +-------------------------+-------------------------+-------------------------+
                        | X                       | Y                       | Z                       |\s
                        +-------------------------+-------------------------+-------------------------+
                        """,
                builder.build()
        );
    }

}
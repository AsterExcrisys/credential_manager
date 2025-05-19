package com.asterexcrisys.acm;

import com.asterexcrisys.acm.services.console.builders.TableBuilder;
import com.asterexcrisys.acm.types.console.CellSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

@SuppressWarnings("unused")
public class TableBuilderTest {

    private TableBuilder builder = new TableBuilder();

    @BeforeEach
    public void setUp() {
        builder = new TableBuilder(CellSize.WRAP_SMALL);
    }

    @Test
    public void shouldBuildTable() {
        builder.addAttribute("A").addAttribute("B").addAttribute("C");
        builder.addRecord(List.of("X", "Y", "Z")).addRecord(List.of("X", "Y", "Z"));
        System.out.println(builder.build().get());
    }

    @Test
    public void shouldBuildTableMultiLine() {
        builder.addAttribute("AAAAAAAAAAAAAAAAAAAAAAAAAA").addAttribute("BBBBBBBBBBBBBBBBBBBBBBBB").addAttribute("CCCCCCCCCCCCCCCCC");
        builder.addRecord(List.of("X", "Y", "Z")).addRecord(List.of("X", "Y", "Z"));
        System.out.println(builder.build().get());
    }

}
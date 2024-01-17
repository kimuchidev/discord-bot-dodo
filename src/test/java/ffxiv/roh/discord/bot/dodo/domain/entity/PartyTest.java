package ffxiv.roh.discord.bot.dodo.domain.entity;

import ffxiv.roh.discord.bot.dodo.domain.exception.CantParseRoleException;
import ffxiv.roh.discord.bot.dodo.domain.party.Role;
import ffxiv.roh.discord.bot.dodo.domain.party.RolePair;
import ffxiv.roh.discord.bot.dodo.domain.party.Roles;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ffxiv.roh.discord.bot.dodo.domain.party.Role.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PartyTest {
    @ParameterizedTest
    @MethodSource("methodSource")
    void doAutoCompositionTest(Roles recruitRoles, List<EntryInfo> entries, List<RolePair> expectedRolePairs, List<String> expectedUnRecruitedUserNames) {
        var party = Party.newInstance("", "", "", LocalDateTime.now(), recruitRoles, "");
        party.setEntryInfoList(entries);
        party.doAutoComposition();

        Assertions.assertThat(party.getRecruitedRolePairs())
                .isEqualTo(expectedRolePairs);

        Assertions.assertThat(party.getNotRecruitedEntries())
                .extracting(EntryInfo::getName)
                .isEqualTo(expectedUnRecruitedUserNames);
    }

    // パラメータのファクトリメソッド
    public static Stream<Arguments> methodSource() throws CantParseRoleException {
        var t = dummyEntryInfo(T);
        var mt = dummyEntryInfo(MT);
        var st = dummyEntryInfo(ST);
        var h = dummyEntryInfo(H);
        var ph = dummyEntryInfo(PH);
        var bh = dummyEntryInfo(BH);
        var d = dummyEntryInfo(D);
        var md = dummyEntryInfo(MD);
        var rd = dummyEntryInfo(RD);
        var cd = dummyEntryInfo(CD);
        var f = dummyEntryInfo(F);
        var hd = dummyEntryInfo(H, D);

        return Stream.of(
                // 単体マッピング

                arguments(Roles.parse("T1"), List.of(t), List.of(new RolePair(T, t)), List.of()), //1
                arguments(Roles.parse("MT1"), List.of(mt), List.of(new RolePair(MT, mt)), List.of()),//2
                arguments(Roles.parse("ST1"), List.of(st), List.of(new RolePair(ST, st)), List.of()), //3
                arguments(Roles.parse("H1"), List.of(h), List.of(new RolePair(H, h)), List.of()),  //4
                arguments(Roles.parse("PH1"), List.of(ph), List.of(new RolePair(PH, ph)), List.of()),//5
                arguments(Roles.parse("BH1"), List.of(bh), List.of(new RolePair(BH, bh)), List.of()),//6
                arguments(Roles.parse("D1"), List.of(d), List.of(new RolePair(D, d)), List.of()), //7
                arguments(Roles.parse("MD1"), List.of(md), List.of(new RolePair(MD, md)), List.of()),//8
                arguments(Roles.parse("RD1"), List.of(rd), List.of(new RolePair(RD, rd)), List.of()),//9
                arguments(Roles.parse("CD1"), List.of(cd), List.of(new RolePair(CD, cd)), List.of()),//10
                arguments(Roles.parse("F1"), List.of(f), List.of(new RolePair(F, f)), List.of()),//11

                // 優先度が高いロールにアサインされる
                //11
                arguments(Roles.parse("T1,H1,D1"), List.of(hd), List.of(
                        new RolePair(T, null),
                        new RolePair(H, hd),
                        new RolePair(D, null)
                ), List.of()),
                //12
                arguments(Roles.parse("T1,H1,D1"), List.of(hd, h), List.of(
                        new RolePair(T, null),
                        new RolePair(H, h),
                        new RolePair(D, hd)
                ), List.of()),

                // 応募が溢れた場合、応募順でアサイン
                //13
                arguments(Roles.parse("T1,H1,D1"), List.of(hd, h, d), List.of(
                        new RolePair(T, null),
                        new RolePair(H, h),
                        new RolePair(D, hd)
                ), List.of("d")),
                //14
                arguments(Roles.parse("T1,H1,D1"), List.of(hd, d, h), List.of(
                        new RolePair(T, null),
                        new RolePair(H, hd),
                        new RolePair(D, d)
                ), List.of("h")),
                //15
                arguments(Roles.parse("T1,H1,D1,F1"), List.of(hd, d, h), List.of(
                        new RolePair(T, null),
                        new RolePair(H, hd),
                        new RolePair(D, d),
                        new RolePair(F, h)
                ), List.of())
        );
    }

    private static EntryInfo dummyEntryInfo(Role... roles) {
        String roleName = Arrays.stream(roles).map(Enum::name).map(String::toLowerCase).collect(Collectors.joining(","));
        return new EntryInfo("id", roleName, List.of(roles));
    }
}
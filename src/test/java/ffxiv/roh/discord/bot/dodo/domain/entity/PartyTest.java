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
import java.util.List;
import java.util.stream.Stream;

import static ffxiv.roh.discord.bot.dodo.domain.party.Role.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PartyTest {
    @ParameterizedTest
    @MethodSource("methodSource")
    void doAutoCompositionTest(Roles recruitRoles, List<EntryInfo> entries, List<RolePair> expectedRolePairs) {
        var party = Party.newInstance("", "", "", LocalDateTime.now(), recruitRoles, "");
        party.setEntryInfoList(entries);
        party.doAutoComposition();

        Assertions.assertThat(party.getRecruitedRolePairs())
                .isEqualTo(expectedRolePairs);
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
                arguments(Roles.parse("T1"), List.of(t), List.of(new RolePair(T, t))),
                arguments(Roles.parse("MT1"), List.of(mt), List.of(new RolePair(MT, mt))),
                arguments(Roles.parse("ST1"), List.of(st), List.of(new RolePair(ST, st))),
                arguments(Roles.parse("H1"), List.of(h), List.of(new RolePair(H, h))),
                arguments(Roles.parse("PH1"), List.of(ph), List.of(new RolePair(PH, ph))),
                arguments(Roles.parse("BH1"), List.of(bh), List.of(new RolePair(BH, bh))),
                arguments(Roles.parse("D1"), List.of(d), List.of(new RolePair(D, d))),
                arguments(Roles.parse("MD1"), List.of(md), List.of(new RolePair(MD, md))),
                arguments(Roles.parse("RD1"), List.of(rd), List.of(new RolePair(RD, rd))),
                arguments(Roles.parse("CD1"), List.of(cd), List.of(new RolePair(CD, cd))),
                arguments(Roles.parse("F1"), List.of(f), List.of(new RolePair(F, f))),

                // 優先度が高いロールにアサインされる
                arguments(Roles.parse("T1,H1,D1"), List.of(hd), List.of(
                        new RolePair(T, null),
                        new RolePair(H, hd),
                        new RolePair(D, null)
                )),
                arguments(Roles.parse("T1,H1,D1"), List.of(hd, h), List.of(
                        new RolePair(T, null),
                        new RolePair(H, h),
                        new RolePair(D, hd)
                )),

                // 応募が溢れた場合、応募順でアサイン
                arguments(Roles.parse("T1,H1,D1"), List.of(hd, h, d), List.of(
                        new RolePair(T, null),
                        new RolePair(H, h),
                        new RolePair(D, hd)
                )),
                arguments(Roles.parse("T1,H1,D1"), List.of(hd, d, h), List.of(
                        new RolePair(T, null),
                        new RolePair(H, hd),
                        new RolePair(D, d)
                )),
                arguments(Roles.parse("T1,H1,D1,F1"), List.of(hd, d, h), List.of(
                        new RolePair(T, null),
                        new RolePair(H, hd),
                        new RolePair(D, d),
                        new RolePair(F, h)
                )),

                arguments(Roles.parse("T1"), List.of(t), List.of(new RolePair(T, t)))
        );
    }

    private static EntryInfo dummyEntryInfo(Role... roles) {
        return new EntryInfo("id", "name", List.of(roles));
    }
}
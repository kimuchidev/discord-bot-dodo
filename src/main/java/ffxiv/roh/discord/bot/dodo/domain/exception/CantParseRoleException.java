package ffxiv.roh.discord.bot.dodo.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 渡された文字列をRoleに変換できないことを示す例外
 */
@RequiredArgsConstructor
@Getter
public class CantParseRoleException extends Exception {
    private final String roleString;
}

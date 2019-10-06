/*
    Copyright 2016-2019 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.willwinder.ugs.nbp.editor.parser.errors;

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import com.willwinder.ugs.nbp.editor.parser.GcodeError;
import com.willwinder.universalgcodesender.gcode.util.Code;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.List;

public class FeedRateMissingErrorParser implements ErrorParser {
    private final FileObject fileObject;
    private int firstFeedRateLine = 0;
    private int firstMovementLine = 0;
    private Token<GcodeTokenId> firstMovementToken;
    private Token<GcodeTokenId> firstFeedRateToken;

    public FeedRateMissingErrorParser(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public void handleToken(Token<GcodeTokenId> token, int line) {
        if (GcodeTokenId.MOVEMENT.equals(token.id())) {
            if ((StringUtils.equalsIgnoreCase(token.text(), Code.G1.name()) ||
                    StringUtils.equalsIgnoreCase(token.text(), Code.G3.name())) && firstMovementToken == null) {
                firstMovementToken = token;
                firstMovementLine = line;
            }
        } else if (GcodeTokenId.PARAMETER.equals(token.id())) {
            if (StringUtils.startsWithIgnoreCase(token.text(), "F") && firstFeedRateToken == null) {
                firstFeedRateToken = token;
                firstFeedRateLine = line;
            }
        }
    }

    @Override
    public List<GcodeError> getErrors() {
        List<GcodeError> errorList = new ArrayList<>();
        if ((firstFeedRateToken == null && firstMovementToken != null) ||
                (firstFeedRateToken != null && firstMovementToken != null && firstMovementLine < firstFeedRateLine)) {
            int offset = firstMovementToken.offset(null);
            GcodeError error = new GcodeError("no-feed-rate", "No feed rate", "No feed rate has been assigned before movement command", fileObject, offset, offset + firstMovementToken.length(), true, Severity.ERROR);
            errorList.add(error);
        }
        return errorList;
    }
}

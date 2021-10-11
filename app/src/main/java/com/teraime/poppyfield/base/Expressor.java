package com.teraime.poppyfield.base;

import android.database.Cursor;
import android.util.Log;

import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;


/**
 * toolset class with
 *
 * Parser and Tokenizer for arithmetic and logic expressions.
 *
 *
 * Part of the Vortex Core classes. 
 *
 * @author Terje Lundin 
 *
 * Teraim Holding reserves the property rights of this Class (2015)
 *
 *
 */

public class Expressor {

    private static Expressor singleton;
    private WorldViewModel model;

    public static Expressor create(WorldViewModel m) {
        if (singleton == null)
            singleton = new Expressor(m);
        return singleton;
    }

    private Expressor(WorldViewModel m) {
        this.model = m;
    }

    private static List<List<String>> targetList = null;

    //Types of tokens recognized by the Engine.
    //Some of these are operands, some functions, etc as denoted by the first argument.
    //Last argument indicates Cardinality or prescedence in case of Operands (Operands are X op Y)
    public enum TokenType {
        function(null, -1),
        booleanFunction(function, -1),
        valueFunction(function, -1),
        has(booleanFunction, 1),
        hasAll(booleanFunction, 1),
        hasMore(booleanFunction, 1),
        hasSome(booleanFunction, 1),
        hasMost(booleanFunction, 1),
        hasSame(booleanFunction, -1),
        hasValue(booleanFunction, -1),
        hasNullValue(booleanFunction, -1),
        photoExists(booleanFunction, 1),
        allHaveValue(booleanFunction, -1),
        not(booleanFunction, 1),
        iff(valueFunction, 3),
        getColumnValue(valueFunction, 1),
        historical(valueFunction, 1),
        hasSameValueAsHistorical(valueFunction, 2),
        getHistoricalListValue(valueFunction, 1),
        getListValue(valueFunction, 1),
        getCurrentYear(valueFunction, 0),
        getCurrentMonth(valueFunction, 0),
        getCurrentDay(valueFunction, 0),
        getCurrentHour(valueFunction, 0),
        getCurrentMinute(valueFunction, 0),
        getCurrentSecond(valueFunction, 0),
        getCurrentWeekNumber(valueFunction, 0),
        getSweDate(valueFunction, 0),
        getStatusVariableValues(valueFunction, 1),
        getGISobjectLength(valueFunction, 0),
        getGISobjectArea(valueFunction, 0),
        getSweRefX(valueFunction, 1),
        getSweRefY(valueFunction, 1),
        getAppName(valueFunction, 0),
        getUserRole(valueFunction, 0),
        getTeamName(valueFunction, 0),
        getUserName(valueFunction, 0),
        export(valueFunction, 4),
        sum(valueFunction, -1),
        concatenate(valueFunction, -1),
        getDelytaArea(valueFunction, 1),
        abs(valueFunction, 1),
        acos(valueFunction, 1),
        asin(valueFunction, 1),
        atan(valueFunction, 1),
        ceil(valueFunction, 1),
        cos(valueFunction, 1),
        exp(valueFunction, 1),
        floor(valueFunction, 1),
        log(valueFunction, 1),
        round(valueFunction, 1),
        sin(valueFunction, 1),
        sqrt(valueFunction, 1),
        tan(valueFunction, 1),
        atan2(valueFunction, 1),
        max(valueFunction, 2),
        min(valueFunction, 2),
        pow(valueFunction, 2),
        unaryMinus(valueFunction, 1),
        variable(null, -1),
        text(variable, 0),
        numeric(variable, 0),
        bool(variable, 0),
        list(variable, 0),
        existence(variable, 0),
        auto_increment(variable, 0),
        none(null, -1),
        literal(null, -1),
        number(null, -1),
        operand(null, 0),
        and(operand, 5),
        or(operand, 4),
        add(operand, 8),
        subtract(operand, 8),
        multiply(operand, 10),
        divide(operand, 10),
        gte(operand, 6),
        lte(operand, 6),
        eq(operand, 6),
        neq(operand, 6),
        gt(operand, 6),
        lt(operand, 6),

        parenthesis(literal, -1),
        comma(literal, -1),
        leftparenthesis(parenthesis, -1),
        rightparenthesis(parenthesis, -1),

        unknown(null, -1),
        startMarker(null, -1),
        endMarker(null, -1),
        ;

        private TokenType parent = null;
        private final List<TokenType> children = new ArrayList<>();
        private final int cardinalityOrPrescedence;

        //only operands has prescedence.
        int prescedence() {
            if (this.parent == operand)
                return cardinalityOrPrescedence;
            else
                return -1;
        }

        TokenType(TokenType parent, int cardinalityOrPrescedence) {
            this.parent = parent;
            if (this.parent != null) {
                this.parent.addChild(this);
            }
            this.cardinalityOrPrescedence = cardinalityOrPrescedence;
        }
        //Methods to extract parent/child relationships.

        private void addChild(TokenType child) {
            children.add(child);
        }

        TokenType getParent() {
            return parent;
        }

        //Normally case is of no consequence
        static TokenType valueOfIgnoreCase(String token) {
            for (TokenType t : TokenType.values()) {
                if (t.name().equalsIgnoreCase(token))
                    return t;
            }
            return null;
        }

    }


    //Operands are transformed into functions. Below a name mapping.
    private final static String[] Operands = new String[]{"=", ">", "<", "+", "-", "*", "/", ">=", "<=", "<>", "=>", "=<"};
    private final static String[] OperandFunctions = new String[]{"eq", "gt", "lt", "add", "subtract", "multiply", "divide", "gte", "lte", "neq", "gte", "lte"};
    private static Logger o;
    private static Map<String, String> currentKeyChain = null;
    private static Set<Variable> variables = null;

}
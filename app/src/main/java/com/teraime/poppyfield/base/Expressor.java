package com.teraime.poppyfield.base;

import android.util.Log;

import androidx.annotation.NonNull;

import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.io.Serializable;
import java.util.ArrayList;
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

        private final TokenType parent;
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


    //marker class
    abstract static class Expr implements Serializable {
        private static final long serialVersionUID = -1968204853256767316L;
        private final TokenType type;
        Expr(TokenType t) {
            type = t;
        }
        TokenType getType() {
            return type;
        }
    }

    public abstract static class EvalExpr extends Expr {
        EvalExpr(TokenType t) {
            super(t);
        }
        abstract Object eval(Map <String,String> myAttrs);
    }

    static class Text extends EvalExpr {
        private final String str;
        Text(Token t) {
            super(TokenType.text);
            this.str=t.str;
        }
        @Override
        String eval(Map <String,String> myAttrs) {
            return str;
        }
        @NonNull
        @Override
        public String toString() {
            return str;
        }
    }

    static class Token implements Serializable {

        private static final long serialVersionUID = -1975204853256767316L;
        String str;
        TokenType type;
        Token(String raw, TokenType t) {
            str=raw;
            type=t;
        }
    }

    private static List<Token> tokenize(String formula) {
        System.out.println("Tokenize this: "+formula);
        List<Token> result= new ArrayList<>();
        char c;
        StringBuilder currToken=new StringBuilder();
        TokenType t = TokenType.none;

        //This is added to support regexp sections that should not be interpreted.
        //Everything within {} will be treated as literals.
        boolean chompAnyCharacter=false;
        boolean inside = false;

        for (int i = 0; i < formula.length(); i++){
            c = formula.charAt(i);

            if (!inside) {
                if (c=='[') {
                    inside = true;
                    add(currToken,TokenType.text,result);
                    currToken.append(c);
                    add(currToken,TokenType.startMarker,result);
                    t = TokenType.none;
                }
                else {
                    t=TokenType.text;
                    currToken.append(c);
                }
                continue;
            }
            if (chompAnyCharacter) {
                if (c=='}') {
                    add(currToken,t,result);
                    t=TokenType.none;
                    chompAnyCharacter=false;

                } else
                    currToken.append(c);

                continue;
            }

            //if a digit, variable or letter comes after an operand, save it.
            if (t == TokenType.operand && (Character.isDigit(c) || Character.isLetter(c)||c=='$')) {
                //save operand.
                add(currToken,t,result);
                //add number.
                t = TokenType.none;
            }
            if (Character.isDigit(c)) {
                if (t == TokenType.none)
                    t = TokenType.number;
                currToken.append(c);
            }
            else if (Character.isLetter(c)) {
                switch (t) {
                    case none:
                    case number:
                        t=TokenType.literal;
                        break;
                }
                currToken.append(c);
            }
            else if (Character.isWhitespace(c)) {
                add(currToken,t,result);
                //Discard whitespace.
                t= TokenType.none;
            }
            else if (c=='$') {
                t = TokenType.variable;
            }
            else if (c=='(' || c==')' || c==',') {
                if (t != TokenType.none){
                    //add any token on left of operand
                    add(currToken,t,result);
                }
                switch (c) {
                    case '(':
                        t=TokenType.leftparenthesis;
                        break;
                    case ')':
                        t=TokenType.rightparenthesis;
                        break;
                    case ',':
                        t=TokenType.comma;
                        break;
                }
                add(c,t,result);
                t= TokenType.none;
            }

            else if (c=='<' || c=='>' || c=='=' || c == '+' || c == '*' || c == '/' || c=='-') {
                if (t != TokenType.none && t != TokenType.operand){
                    //add any token on left of operand
                    add(currToken,t,result);
                }
                //unary minus
                //System.out.println("Found zunary operator "+c+" i "+i);
                if ((t == TokenType.operand || (t == TokenType.none&&(i==1||result.get(result.size()-1).type==TokenType.leftparenthesis || result.get(result.size()-1).type==TokenType.operand))) && (c =='-') && (i+1)!=formula.length() && (!Character.isWhitespace(formula.charAt(i+1)))) {
                    //if ((t == TokenType.operand || (t == TokenType.none&&(i==1||result.get(result.size()-1).type==TokenType.leftparenthesis))) && (c =='-') && (i+1)!=formula.length() && (!Character.isWhitespace(formula.charAt(i+1)))) {
                    //System.out.println("Found unary operator "+c);
                    //System.out.println("Currtorken: "+currToken.toString());
                    add(currToken,t,result);
                    currToken.append(c);
                    t=TokenType.unaryMinus;
                    add(currToken,t,result);
                    t=TokenType.none;
                } else {
                    currToken.append(c);
                    t=TokenType.operand;
                }
            }
            else if (c=='{') {
                //all characters now treated as being literal.
                add(currToken,t,result);
                t=TokenType.literal;
                chompAnyCharacter=true;
            }
            else if (c==']') {
                add(currToken,t,result);
                currToken.append(c);
                add(currToken,TokenType.endMarker,result);

                inside = false;
            }

            else {
                currToken.append(c);
                //System.out.println("unrecognized: "+c+" AT POS "+i+" in "+formula+" chomp: "+chompAnyCharacter);
            }
        }
        if (inside) {
            System.err.println("Missing end bracket");
            return null;
        }
        //System.out.println("Reached end of tokenizer. CurrentToken is "+currToken+" and t is "+t.name());

        if (t != TokenType.none)
            add(currToken,t,result);

        return result;
    }


    private static void add(char c, TokenType t, List<Token> result) {
        result.add(new Token(String.valueOf(c),t));
    }


    private static void add(StringBuilder currToken, TokenType t,List<Token> result) {
        //need to change tokentype if literal and keyword.
        if (currToken.length()!=0)
            result.add(new Token(currToken.toString(),t));
        currToken.setLength(0);
    }

    //check rules between token pairs.
    private static boolean testTokens(List<Token> result) {
        //Rule 1: op op
        o = Logger.gl();
        boolean valueF=false,booleanF=false;
        Token current=null,prev=null;
        int pos=-1,lparC=0,rparC=0;
        for (Token t:result) {
            pos++;
            if (t.type==TokenType.text) {
                //Skipp text.
                continue;
            }
            //check number of parenthesis...
            if (t.type.getParent()==TokenType.parenthesis) {
                if (t.type==TokenType.rightparenthesis)
                    rparC++;
                else
                    lparC++;
            }
            if (current==null && prev == null) {
                prev=t;
                continue;
            }
            else if (current == null) {
                current = t;
            }
            else {
                prev = current;
                current = t;
            }

            //try to find supported functions and change to correct type.
            if (prev.type==TokenType.literal) {

                //Check for PI
                if (prev.str.equals("PI")) {
                    prev.type=TokenType.number;
                    prev.str=Double.toString(Math.PI);
                    //System.out.println("Found PI!"+prev.str);
                    continue;
                }

                TokenType x = TokenType.valueOfIgnoreCase(prev.str);

                //check if AND OR
                if (isLogicalOperand(x))
                    prev.type=TokenType.operand;
                else
                    //check if function
                    if (current.type == TokenType.leftparenthesis) {
                        if (x==null) {
                            o.e("Syntax Error: Function "+prev.str+" does not exist!");
                            return false;
                        }
                        if (isFunction(x)) {
                            TokenType parent = x.getParent();
                            //System.out.println("found function match : "+prev.str);
                            prev.type = x;
                            //Check that there aren't both logical and value functions in the same expression.
                            //System.out.println("parent: "+parent);
                            if (parent == TokenType.valueFunction)
                                valueF=true;
                            if (parent == TokenType.booleanFunction)
                                booleanF = true;
                        } else {
                            o.e("The token "+prev.str+" is used as function, but is in fact a "+prev.type);
                            return false;
                        }
                    }
            }

            else if (prev.type==TokenType.operand) {
                boolean found=false;
                for (int i=0;i<Operands.length;i++) {
                    if(prev.str.equalsIgnoreCase(Operands[i])) {
                        prev.str=OperandFunctions[i];
                        //System.out.println("Replaced "+Operands[i]+" with corresponding operand function: "+prev.str);
                        found =true;
                    }

                }
                if (!found) {
                    o.e("Syntax Error: Operator "+prev.str+" does not exist.");
                    System.err.println("Syntax Error: Operator "+prev.str+" does not exist.");
                    return false;
                }
            }
            //if (prev.type == current.type && current.type.getParent()!=TokenType.parenthesis) {
            //	System.err.println("Rule 1. Syntax does not allow repetition of same type at token "+pos+": "+prev.str+":"+current.str);
            //	return false;
            //}
        }
        //Check for unbalanced paranthesis
        if (lparC!=rparC) {
            o.e("Unequal number of left and right parenthesis. Left: "+lparC+" right: "+rparC);
            System.err.println("Rule 2. Equal number of left and right parenthesis. Left: "+lparC+" right: "+rparC);
            return false;
        }
        //Check for mix between data types
        //if (valueF&&booleanF) {
        //	System.err.println("Rule 3. Both logical(true-false) and value functions present. This is not allowed");
        //	return false;
        //}
        return true;
    }

    private static boolean isLogicalOperand(TokenType x) {
        if (x==null)
            return false;
        String name = x.name();
        return ("AND".equalsIgnoreCase(name) || "OR".equalsIgnoreCase(name));
    }


    private static boolean  isFunction(TokenType t) {
        TokenType parent = t.getParent();
        return (parent !=null && parent.getParent() == TokenType.function);
    }

    private static class StreamAnalyzer {
        final Iterator<Token> mIterator;
        List<Token> curr;
        int depth = 0;
        Map <String,String> myAttrs;
        StreamAnalyzer(List<Token> tokens, Map <String,String> mAttrs) {
            mIterator = tokens.iterator();
            curr=null;
            myAttrs = mAttrs;
        }
        boolean hasNext() {
            return mIterator.hasNext();
        }
        EvalExpr next() {
            while (mIterator.hasNext()) {
                Token t = mIterator.next();
                if (t.type==TokenType.text)
                    return new Text(t);
                if (curr!=null&&t.type==TokenType.leftparenthesis)
                    depth++;
                if (curr!=null&&t.type==TokenType.rightparenthesis)
                    depth--;
                if (t.type==TokenType.startMarker) {
                    curr = new ArrayList<>();
                    //new token either if endmarker, or a comma on toplevel.
                } else if (t.type==TokenType.endMarker || (t.type==TokenType.comma && depth==0)) {
                    if (curr!=null && !curr.isEmpty()) {
                        //Log.d("franco","CURR tokens: ");
                        //printTokens(curr);
                        EvalExpr ret = analyzeExpression(curr,myAttrs);
                        if (ret==null)
                            System.err.println("Eval of expression "+curr.toString()+" failed");
                        curr = new ArrayList<>();
                        return ret;
                    } else {
                        System.err.println("Empty Expr or missing startTag.");
                        return null;
                    }
                }
                if (curr!=null)
                    curr.add(t);
                else
                    System.err.println("Discarded "+t.str);
            }
            System.err.println("Missing end marker for Expr ']'");
            return null;
        }

        String getFaultyTokens() {
            StringBuilder sres=new StringBuilder();
            for (Token c:curr) {
                sres.append(c.toString());
            }
            return sres.toString();
        }
    }

    static private class ExpressionAnalyzer {
        //stream to use
        private final Iterator<Token>it;
        Map <String,String> myAttrs;
        ExpressionAnalyzer(Iterator<Token> iterator, Map <String,String> mAttrs) {
            it = iterator;
            myAttrs=mAttrs;
        }


        boolean hasNext() {
            return it.hasNext();
        }
        Expr next() {
            Token t;
            if (it.hasNext()) {
                t = it.next();
                //System.out.println("In next: "+t.type);
                assert(t!=null);
                TokenType type = t.type;

                switch (type) {
                    case leftparenthesis:
                        return new Push();
                    case rightparenthesis:
                        return new Pop();
                    case variable:
                    case number:
                    case literal:
                    case comma:
                        return new Atom(t,myAttrs);
                    case operand:
                        return new Operand(t);
                    case text:
                        return new Text(t);

                }
                TokenType p = type.parent;
                if (isFunction(type)) {
                    return new Function(type, it,myAttrs);
                }
            }

            return null;

        }
    }

    private static class Push extends Expr {
        Push() {
            super(null);
        }
        @Override
        public String toString() {
            return "parenthesis";
        }
    }
    private static class Pop extends Expr {
        Pop() {
            super(null);
        }
        @Override
        public String toString() {
            return "parenthesis";
        }
    }

    private static class Operand extends Expr {
        final Token myToken;
        Operand(Token t) {
            super(t.type);
            myToken = t;
        }
        @Override
        public String toString() {
            return myToken.str;
        }
    }

    public static class Atom extends EvalExpr {
        final Token myToken;
        Map <String,String> myAttrs;
        Atom(Token t,Map <String,String> mAttrs) {
            super (t.type);
            myToken = t;
            myAttrs=mAttrs;
        }
        @Override
        public String toString() {
            if (myToken!=null)
                return myToken.str;
            else
                return null;
        }

        public boolean isVariable() {
            return (getType()==TokenType.variable);
        }

        public Object eval(Map <String,String> myAttrs) {
            //Log.d("vortex","In eval for Atom type "+type);

            switch(getType()) {
                case variable:
                    String value = myAttrs.get(myToken.str);
                    if (value==null ) {
                        System.out.println("Variable '"+this.toString()+"' does not have a value or Variable is missing.");
                        return null;
                    }

                    //Log.d("vortex","Atom variable ["+v.getId()+"] Type "+v.getType()+" Value: "+value);

                    if (Tools.isNumeric(value)) {
                        Log.d("vortex","numeric");
                        double d = Double.parseDouble(value);
                        if (value.contains(".") || d>Integer.MAX_VALUE || d<Integer.MIN_VALUE)
                            return d;
                        else
                            return Integer.parseInt(value);
                    }
                        if (value.equalsIgnoreCase("false")) {
                            Log.d("vortex","Returning false");
                            return false;
                        }
                        if (value.equalsIgnoreCase("true")) {
                            Log.d("vortex","Returning true");
                            return true;
                        }
                    Log.d("vortex","literal");
                    if (value.isEmpty()) {
                        Log.e("vortex","empty literal...returning null");
                        return null;
                    }
                    return value;
                case number:
                    //Log.d("vortex","this is a numeric atom");
                    if (myToken !=null && myToken.str!=null) {
                        //	System.out.println("Numeric value: "+myToken.str);
                        if (myToken.str.contains("."))
                            return Double.parseDouble(myToken.str);
                        else
                            return Integer.parseInt(myToken.str);
                    }
                    else {
                        System.err.println("Numeric value was null");
                        return null;
                    }
                case literal:
                    //Log.d("vortex","this is a literal atom");
                    if (myToken.str.equalsIgnoreCase("false"))
                        return false;
                    else if (myToken.str.equalsIgnoreCase("true"))
                        return true;
                    else
                        return toString();

                default:
                    System.err.println("Atom type has no value: "+this.getType());
                    return null;
            }
        }
    }

    private static class Convoluted extends EvalExpr {
        final EvalExpr arg1,arg2;
        final Operand operator;

        Convoluted(Expr newArg, Expr existingArg, Operand operator) {
            super(null);
            this.arg1 = (EvalExpr) existingArg;
            this.arg2 = (EvalExpr) newArg;
            this.operator=operator;
        }
        @Override
        public String toString() {
            String arg1s = arg1.toString();
            String arg2s = arg2.toString();
            if (arg1s==null)
                arg1s="?";
            if (arg2s==null)
                arg2s="?";
            return String.format("%s(%s,%s)", operator.toString(), arg1s, arg2s);
        }

        public Object eval(Map <String,String> myAttrs)  {
            //Log.d("vortex","In eval for convo");
            Object arg1v = arg1.eval(myAttrs);


            if (arg1v==null) {
                String opS =operator.myToken.str;
                if (opS!=null) {
                    TokenType op = TokenType.valueOfIgnoreCase(opS);
                    if (op != null && op.equals(TokenType.or)) return arg2.eval(myAttrs);
                }
                return null;
            }

            Object arg2v = arg2.eval(myAttrs);

            Log.e("vortex",(arg1v.toString())+ " " + operator.myToken.str+" "+((arg2v==null)?"null":arg2v.toString()));
            if (arg2v==null) {
                Log.e("vortex","Arg2 is null! Operator is "+operator.myToken.str);
                String opS =operator.myToken.str;
                if (opS!=null) {
                    TokenType op = TokenType.valueOfIgnoreCase(opS);
//					Log.d("vortex","op is "+op+" which equals and? "+op.equals(TokenType.and));
                    if (op != null) {
                        if (op.equals(TokenType.or)) {
                            if (arg1v instanceof Boolean)
                                if ((Boolean) arg1v) {
                                    Log.d("vortex","arg1 is true so returning true");
                                    return true;
                                } else
                                    Log.d("vortex","arg1 is false");
                        }
                        else if (op.equals(TokenType.and)) {
//								Log.d("vortex","operator is AND! Arg1: "+arg1v);
                            if (arg1v instanceof Boolean)
                                if (!((Boolean) arg1v))
                                    return false;
                        }
                    }
                }
                Log.d("vortex","...returning null");
                return null;
            }

            //functions require both arguments be of same kind.

            boolean isNumericOperator = Tools.isNumeric(arg1v) && Tools.isNumeric(arg2v);
            boolean isBooleanOperator = arg1v instanceof Boolean
                    && arg2v instanceof Boolean;
            //System.err.println("arg1: "+arg1v+" arg2: "+arg2v+ "arg1vClass: "+arg1v.getClass()+" arg2vClass: "+arg2v.getClass());
            //Requires Double arguments.
            try {
                if (isNumericOperator) {
                    double arg1F, arg2F;
                    Object res = null;
                    arg1F = castToDouble(arg1v);
                    arg2F = castToDouble(arg2v);
/*
					if (isDoubleOperator) {
						arg1F = ((Double) arg1v).doubleValue();
						arg2F = ((Double) arg2v).doubleValue();
					} else {
						if (arg1v instanceof Integer)
							arg1F = ((Integer) arg1v).doubleValue();
						else
							arg1F = (Double) arg1v;
						if (arg2v instanceof Integer)
							arg2F = ((Integer) arg2v).doubleValue();
						else
							arg2F = (Double) arg2v;
					}
*/
                    String opS = operator.myToken.str;
                    if (opS != null) {
                        TokenType op = TokenType.valueOf(opS);
                        switch (op) {

                            case add:
                                res =  (arg1F + arg2F);
                                break;
                            case subtract:
                                res = (arg1F - arg2F);
                                break;
                            case multiply:
                                res = (arg1F * arg2F);
                                break;
                            case divide:
                                res =  (arg1F / arg2F);
                                break;
                            case eq:
                                res = arg2F == arg1F;
                                Log.e("vortex", "arg1F eq arg2F? " + arg1F + " eq " + arg2F + ": " + res);
                                break;
                            case neq:
                                res = arg1F != arg2F;
                                Log.e("vortex", "arg1F neq arg2F? " + arg1F + " neq " + arg2F + ": " + res);
                                break;
                            case gt:
                                res = arg1F > arg2F;
                                break;
                            case lt:
                                res = arg1F < arg2F;
                                break;
                            case lte:
                                res = arg1F <= arg2F;
                                break;
                            case gte:
                                res = arg1F >= arg2F;
                                break;
                            default:
                                System.err.println("Unsupported operand: " + op);

                                o.e("Unsupported arithmetic operator: " + op);
                                break;
                        }
                    } else {
                        System.err.println("Unsupported arithmetic operand: " + operator.getType());

                        o.e("Unsupported arithmetic operand: " + operator.getType());
                    }
                    Log.e("vortex","RESULT: "+res);
                    return res;
                }

                //Requires boolean arguments.
                else if (isBooleanOperator) {
                    Boolean arg1B,arg2B,res=null;

                    arg1B=(Boolean)arg1v;
                    arg2B=(Boolean)arg2v;
                    String opS =operator.myToken.str;
                    if (opS!=null) {
                        TokenType op = TokenType.valueOfIgnoreCase(opS);
                        if (op != null) {
                            switch (op) {
                                case or:
                                    res = (arg1B||arg2B);
                                    //Log.e("vortex","OR Evaluates to "+res+" for "+arg1B+" and "+arg2B);
                                    break;
                                case and:
                                    //System.err.println("Gets to and");
                                    res = (arg1B&&arg2B);
                                    break;
                                case eq:
                                    res = (arg1B==arg2B);
                                    break;
                                default:

                                    System.err.println("Unsupported boolean operand: "+op);

                                    o.e("Unsupported boolean operator: "+op);
                                    break;
                            }
                        }

                    }
                    return res;
                    // if not boolean and not numeric it is literal.
                } else  {
                    String arg1S=arg1v.toString();
                    String arg2S=arg2v.toString();
                    TokenType op = TokenType.valueOfIgnoreCase(operator.myToken.str);
                    //System.out.println("in isliteral with exp: "+arg1S+" "+operator.myToken.str+" "+arg2S);
                    o.d("EXPR","calculating literal expression "+arg1S+" "+operator.myToken.str+" "+arg2S);

                    if (op != null) {
                        switch (op) {
                            case add:
                        /*
                        if (tret==null) {
                            tret ="foock";
                            Log.d("vortex","first so returning "+arg1S+arg2S);
                            return arg1S+arg2S;
                        } else
                        */
                                return arg1S+arg2S;

                            case eq:
                                return arg1S.equals(arg2S);
                            case neq:
                                return !arg1S.equals(arg2S);
                            default:
                                System.err.println("Unsupported literal operand: "+op);

                                o.e("Unsupported literal operator: "+op+" a1: "+arg1S+" a2: "+arg2S);
                                break;
                        }
                    }
                }
            } catch (ClassCastException e) {
                Log.d("vortex","Classcast exception for expression "+this.toString()+"arg1: "+arg1v);

                o.e("Illegal arguments (wrong type) in expression: " +this.toString()+". Missing $ operator?");

            }
            return null;
        }

        private double castToDouble(Object arg) {
            if (arg instanceof Double)
                return (Double) arg;
            if (arg instanceof Integer)
                return ((Integer) arg).doubleValue();
            if (arg instanceof Float)
                return ((Float) arg).doubleValue();
            if (arg instanceof String)
                return Double.parseDouble((String)arg);
            o.e("I never get here...Object is a "+arg.getClass());
            return -1;
        }

    }

    private static class Function extends EvalExpr {
        //try to build a function from the tokens in the beg. of the given token stream.
        private static final int No_Null = 1;
        private static final int No_Null_Numeric=2;
        private static final int No_Null_Literal=3;
        private static final int NO_CHECK = 4;
        private static final int Null_Numeric = 5;
        private static final int Null_Literal = 6;
        private static final int No_Null_Boolean = 7;
        private static final int Null_Boolean = 8;

        private final List<EvalExpr> args = new ArrayList<>();

        Function(TokenType type, Iterator<Token> it, Map <String,String> myAttrs) {
            super(type);
            //iterator reaches end?
            int depth=0;
            Token e;
            final List<List<Token>> argsAsTokens = new ArrayList<>();
            List<Token> funcArg = new ArrayList<>();
            boolean argumentReady=false;


            while(it.hasNext()) {
                e = it.next();
                //system.out.println("Expr "+e.type);

                if (e.type==TokenType.leftparenthesis) {
                    depth++;
                    //system.out.println("+ depth now "+depth);
                    //discard paranthesis...not used.
                    if (depth==1)
                        continue;
                } else if (e.type==TokenType.rightparenthesis) {
                    depth--;
                    //system.out.println("- depth now "+depth);
                    if (depth==0)
                        argumentReady=true;
                }  else if (e.type==TokenType.comma && depth==1 ) {
                    argumentReady = true;
                }

                if (!argumentReady) {
                    //System.out.println("Added "+e.str+" to funcArg. I am  "+type);
                    funcArg.add(e);
                    if (depth==0 && type == TokenType.unaryMinus) {
                        //System.out.println("Found argument for unary f "+funcArg.get(0).str+" l "+funcArg.size());
                        argumentReady = true;
                    }
                }

                if (argumentReady) {
                    if (!funcArg.isEmpty()) {
                        argsAsTokens.add(funcArg);
                        funcArg= new ArrayList<>();
                    }
                    //					else
                    //						;
                    //system.out.println("No argument in function "+type.name());


                    if (e.type==TokenType.rightparenthesis || type==TokenType.unaryMinus)
                        break;
                    else
                        argumentReady = false;

                }
            }
            if (!argumentReady) {
                System.err.println("Missing closing paranthesis in function "+type.name());
                return;
                //printTokens(funcArg);
            }
            //recurse for each argument.
            int i=1;
            for (List<Token> arg:argsAsTokens) {
                //system.out.println("Recursing over argument "+i++ +"in function "+type.name()+" :");
                //printTokens(arg);
                EvalExpr analyzedArg;
//				try {
                analyzedArg = analyzeExpression(arg,myAttrs);
                if (analyzedArg != null) {
                    args.add(analyzedArg);
                } else {
                    System.err.println("Fail to parse: ");
                    printTokens(arg);
                }
					/*
				} catch (ExprEvaluationException e1) {
					System.err.println("Fail to parse :");
					printTokens(arg);
				}
				*/

            }
        }

        @Override
        public Object eval(Map <String,String> myAttrs) {

            //Log.d("vortex","Function eval: "+getType());

            Object result=null;
            List<Object> evalArgs = new ArrayList<>();
            int j=0;
            double arg1F=0,arg2F=0;
            for (EvalExpr arg:args) {
                result = arg.eval(myAttrs);
                evalArgs.add(result);
                if (j==0) {
                    if (result instanceof Integer)
                        arg1F = ((Integer) result).doubleValue();
                    if (result instanceof Double)
                        arg1F = (Double) result;
                }
                else if (j==1) {
                    if (result instanceof Integer)
                        arg2F = ((Integer) result).doubleValue();
                    if (result instanceof Double)
                        arg2F = (Double) result;
                }
                j++;

            }


            boolean gH=false;


            //Now all arguments are evaluated. Execute function!

            switch (getType()) {

                case max:
                    if (checkPreconditions(evalArgs,2,No_Null_Numeric))
                        return Math.max(arg1F, arg2F);
                    break;
                case abs:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.abs(arg1F);
                    break;
                case acos:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.acos(arg1F);
                    break;
                case asin:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.asin(arg1F);
                    break;
                case atan:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.atan(arg1F);
                    break;
                case ceil:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.ceil(arg1F);
                    break;
                case cos:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.cos(arg1F);
                    break;
                case exp:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.exp(arg1F);
                    break;
                case floor:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.floor(arg1F);
                    break;
                case log:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.log(arg1F);
                    break;
                case round:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.round(arg1F);
                    break;
                case sin:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.sin(arg1F);
                    break;
                case sqrt:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.sqrt(arg1F);
                    break;
                case tan:
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric))
                        return Math.tan(arg1F);
                    break;
                case atan2:
                    if (checkPreconditions(evalArgs,2,No_Null_Numeric))
                        return Math.atan2(arg1F,arg2F);
                    break;
                case min:
                    if (checkPreconditions(evalArgs,2,No_Null_Numeric))
                        return Math.min(arg1F,arg2F);
                    break;
                case pow:
                    if (checkPreconditions(evalArgs,2,No_Null_Numeric))
                        return Math.pow(arg1F,arg2F);
                    break;

                case iff:
                    if (checkPreconditions(evalArgs,3,NO_CHECK)) {
                        if (evalArgs.get(0) instanceof Boolean) {
                            if ((Boolean)evalArgs.get(0))
                                return evalArgs.get(1);
                            else
                                return evalArgs.get(2);
                        }
                    }
                    break;
                case unaryMinus:
                    //Log.d("vortex","In function unaryminus");
                    if (checkPreconditions(evalArgs,1,No_Null_Numeric)){
                        Log.d("vortex","returning: "+ (-(Integer)evalArgs.get(0)));
                        return -((Integer)evalArgs.get(0));
                    }
                    break;
                case not:
                    //                   Log.d("vortex","in function not with evalArgs: "+evalArgs);
                    if (checkPreconditions(evalArgs,1,Null_Boolean)) {
//                        Log.d("vortex","evalArgs.get0 is "+evalArgs.get(0)+" type "+evalArgs.get(0).getClass().getSimpleName());
                        return evalArgs.get(0)==null?null:!((Boolean)evalArgs.get(0));

                    }
                    break;

                case getCurrentYear:
                    return Clock.getYear();
                case getCurrentMonth:
                    return Clock.getMonth();
                case getCurrentDay:
                    return Clock.getDayOfMonth();
                case getCurrentHour:
                    return Clock.getHour();
                case getCurrentMinute:
                    return Clock.getMinute();
                case getCurrentSecond:
                    return Clock.getSecond();
                case getCurrentWeekNumber:
                    return Clock.getWeekNumber();
                case getSweDate:
                    return Clock.getSweDate();
                case getColumnValue:
                    if (checkPreconditions(evalArgs,1,No_Null_Literal)) {
                        Log.d("EXPR","eval args in getcol"+(evalArgs==null?"null":evalArgs.toString()));
                            return myAttrs.get((String) evalArgs.get(0));

                    }
                    break;
                case getUserName:
                    return "tony";//this.getGlobalPrefs().getString(PersistenceHelper.USER_ID_KEY,"Tony");
                case sum:
                    if (!checkPreconditions(evalArgs,-1,Null_Numeric))
                        return 0;
                    else {
                        Object sum = 0;
                        int intSum = 0; double doubleSum = 0;
                        for (Object arg : evalArgs) {
                            if (arg!=null) {
                                if (arg instanceof Integer)
                                    intSum += (Integer) arg;
                                else if (arg instanceof Double)
                                    doubleSum += (Double) arg;
                            }

                        }
                        if (doubleSum > 0)
                            return (double)intSum+doubleSum;
                        else
                            return intSum;
                    }
                case concatenate:
                    if (!checkPreconditions(evalArgs,-1,Null_Literal)) {
                        return null;
                    }
                    else {
                        StringBuilder stringSum= new StringBuilder();
                        for (Object arg : evalArgs) {
                            if (arg!=null)
                                stringSum.append(arg);
                        }
                        return stringSum.toString();
                    }
                case hasNullValue:
                    return !checkPreconditions(evalArgs,-1,No_Null);

                default:
                    System.err.println("Unimplemented function: "+getType().toString());
                    break;


            }
            return null;
        }
        /*
        private Boolean booleanValue(Object obj) {
            if (obj==null)
                return (Boolean)null;
            if (obj instanceof String) {
                if (obj.equals("true")||obj.equals("1")||obj.equals("1.0"))
                    return true;
                if (obj.equals("false")||obj.equals("0")||obj.equals("0.0"))
                    return false;



            } else if (obj instanceof Double) {
                if ((Double)obj==1.0d)
                    return true;
                if ((Double)obj==0.0d)
                    return false;

            }
            Log.e("vortex","no boolean value found for "+obj);
            o.e("no boolean value found for "+obj);
            return null;
        }
         */
        private boolean checkPreconditions(List<Object> evaluatedArgumentsList,int cardinality, int flags) {
            if ((flags==No_Null || flags== No_Null_Numeric || flags == No_Null_Literal || flags == No_Null_Boolean)
                    && evaluatedArgumentsList.contains(null)) {
                //
                //o.e("Argument in function '"+getType().toString()+"' is null, but function does not allow NULL arguments.");
                Log.e("Vortex","Argument in function '"+getType().toString()+"' is null");

                return false;
            }
            if (cardinality!=-1 && cardinality!=evaluatedArgumentsList.size()) {
                o.e("Too many or too few arguments for function '"+getType().toString()+"'. Should be "+cardinality+" argument(s), not "+evaluatedArgumentsList.size()+"!");
                Log.e("Vortex","Too many or too few arguments for function '"+getType().toString()+"'. Should be "+cardinality+" argument(s), not "+evaluatedArgumentsList.size()+"!");
                return false;
            }
            if (flags== No_Null_Numeric) {
                for (Object obj:evaluatedArgumentsList) {
                    if ((obj instanceof Double)||(obj instanceof Integer)||(obj instanceof Float)) {
                        continue;
                    } else {
                        o.e("Type error. Non numeric argument for function '"+getType().toString()+"'. Argument is a "+obj.getClass().getSimpleName());
                        Log.e("Vortex","Type error. Non numeric argument for function '"+getType().toString()+"'. Argument is a "+obj.getClass().getSimpleName());
                        return false;
                    }

                }

            }
            if (flags == No_Null_Literal) {
                for (Object obj:evaluatedArgumentsList) {
                    if (!(obj instanceof String)) {
                        o.e("Type error. Non literal argument for function '" + getType().toString() + "'.");
                        Log.e("Vortex","Type error. Non literal argument for function '"+getType().toString()+"'.");
                        return false;
                    }
                }
            }
            if (flags == Null_Numeric) {
                for (Object obj:evaluatedArgumentsList) {
                    //Log.d("vortex","In null_numeric with "+obj);
                    if (obj !=null && !(obj instanceof Double)&&!(obj instanceof Integer)&&!(obj instanceof Float)) {

                        o.e("Type error. Not null & not numeric argument for function '" + getType().toString() + "'. Argument evaluated to : "+obj+" Type: "+obj.getClass().getName());
                        Log.e("Vortex","Type error. Not null & not numeric argument for function '"+getType().toString()+"'.");
                        return false;
                    }
                }
            }
            if (flags == Null_Literal) {
                for (Object obj:evaluatedArgumentsList) {
                    if (obj !=null && !(obj instanceof String)) {

                        o.e("Type error. Not null & Non literal argument for function '" + getType().toString() + "'. Argument evaluated to : "+obj+" Type: "+obj.getClass().getName());
                        Log.e("Vortex","Type error. Not null & Non literal argument for function '"+getType().toString()+"'.");
                        return false;
                    }
                }
            }
            if (flags == No_Null_Boolean) {
                for (Object obj:evaluatedArgumentsList) {
                    if (!(obj instanceof Boolean)) {
                        Log.e("Vortex","Type error. Non boolean argument for function '"+getType().toString()+ "'. Argument evaluated to : "+obj+" Type: "+obj.getClass().getName());

                        o.e("Type error. Non boolean argument for function '" + getType().toString() + "'. Argument evaluated to : "+obj+" Type: "+obj.getClass().getName());
                        return false;
                    }
                }
            }
            if (flags == Null_Boolean) {
                for (Object obj:evaluatedArgumentsList) {
                    if (obj !=null && !(obj instanceof String || !obj.equals("true") || !obj.equals("false") ) ) {

                        o.e("Type error. Non boolean argument for function '" + getType().toString() + "'.");
                        Log.e("Vortex","Type error. Not null & Non boolean argument for function '"+getType().toString()+"'.");
                        return false;
                    }
                }
            }

            return true;

        }



        @Override
        public String toString() {
            return getType().name()+"("+args.toString()+")";
        }


    }


    private static EvalExpr analyzeExpression(List<Token> tokens, Map<String,String> myAttrs) {
        boolean err = false;
        // Operation stack.
        Stack<Expr> opStack = new Stack<>();
        // Value stack.
        Stack<Expr> valStack = new Stack<>();

        // empty expr
        if (tokens == null || tokens.isEmpty())
            return null;

        ExpressionAnalyzer ef = new ExpressionAnalyzer(tokens.iterator(),myAttrs);

        Expr e = null, top;
        //System.out.println("Before:  " + tokens);

        while (!err && ef.hasNext()) {
            if (e == null)
                e = ef.next();
            if (e == null) {
                System.out.println("continue on null");
                continue;
            }

            //System.out.println("vs: " + valStack);
            //System.out.println("os: " + opStack);
            //System.out.println("e: " + e);

            if (e instanceof Push) {
                opStack.push(e);
            } else if (e instanceof Pop) {
                while (!opStack.isEmpty() && opStack.peek() instanceof Operand) {
                    valStack.push(new Convoluted(valStack.pop(),
                            valStack.pop(), (Operand) opStack.pop()));
                }
                if (!opStack.isEmpty() && opStack.peek() instanceof Push) {
                    opStack.pop();
                } else {
                    System.err.println("Error: unbalanced parenthesis.");
                    err = true;
                }
            } else if (e instanceof Operand) {
                // Stack empty? Then push.

                if (opStack.isEmpty()) {
                    //System.out.println("empty->push");
                    opStack.push(e);
                } else {
                    //System.out.println("Top of stack: " + opStack.peek());
                    int operatorPrecedence = Objects.requireNonNull(TokenType.valueOfIgnoreCase(
                            e.toString())).prescedence();
                    int topStackPrecedence = Objects.requireNonNull(TokenType.valueOfIgnoreCase(
                            opStack.peek().toString())).prescedence();
                    // This has higher precedence? Then push.
                    if (operatorPrecedence > topStackPrecedence) {
                        //System.out.println("precedence->push");
                        opStack.push(e);
                    } else {
                        //System.out.println("calctop");
                        if (valStack.size() < 2) {
                            System.err.println("smallstack: " + valStack);
                            err = true;
                        }
                        // Evaluate until stack empty or precedence of stack
                        // lower than this op.
                        else
                            valStack.push(new Convoluted(valStack.pop(),
                                    valStack.pop(), (Operand) opStack.pop()));

                        // use same operator.
                        continue;
                    }

                }

            } else {
                //System.out.println("Pushing: " + e + " of class "
                //		+ e.getClass());
                valStack.push(e);
            }
            e = null;
        }
        //If any items remain on stack, add them.
        while (!opStack.isEmpty() && opStack.peek() instanceof Operand && valStack.size()>1) {
            valStack.push(new Convoluted(valStack.pop(),
                    valStack.pop(), (Operand) opStack.pop()));
        }
        //System.out.println("Returning: " + ret);
        return valStack.isEmpty()?null:(EvalExpr) valStack.pop();
    }

    public static String analyze(List<EvalExpr> expressions,Map <String,String> myAttrs) {
        if (expressions == null) {
            Logger.gl().e("Expression was null in Analyze. This is likely due to a syntax error in the original formula");
            return null;
        }
        //evaluate in default context.
        Log.d("franco","Analyzing "+expressions.toString()+"with context "+(myAttrs==null?"null":myAttrs.toString()));
        StringBuilder endResult = new StringBuilder();
        for (EvalExpr expr:expressions) {
            //tret=null;
            Object rez;
            //System.out.println("Analyze: "+expr.toString());
            rez = expr.eval(myAttrs);
            if (rez!=null) {
                //System.out.println("Part Result "+rez.toString());
                endResult.append(rez);
            } else
                System.err.println("Got null back when evaluating "+expr.toString()+" . will not be included in endresult.");

        }

        //Log.d("franco",expressions.toString()+" -->  "+endResult.toString());
        if (endResult.toString().isEmpty())
            return null;
        else
            return endResult.toString();
    }

    //No Context
    public static List<EvalExpr> preCompileExpression(String expression) {
        return preCompileExpression(expression,null);
    }

    //Context
    public static List<EvalExpr> preCompileExpression(String expression,Map <String,String> myAttrs) {
        if (expression==null) {
            Log.e("vortex","Precompile expression returns immediately on null string input");
            return null;
        }
        o = Logger.gl();
        Log.d("EXPR","Precompiling: "+expression);
        List<Token> result = tokenize(expression);
        //printTokens(result);
        List<EvalExpr> endResult = new ArrayList<>();
        if (result!=null && testTokens(result)) {
            StreamAnalyzer streamAnalyzer = new StreamAnalyzer(result,myAttrs);
            while (streamAnalyzer.hasNext()) {

                EvalExpr rez=null;
                rez = streamAnalyzer.next();
                if (rez!=null) {
                    endResult.add(rez);
                } else {
                    o.e("Subexpr evaluated to null while evaluating "+expression);
                    System.err.println("Tokenstream evaluated to null for: "+streamAnalyzer.getFaultyTokens());
                }
            }
            if (endResult.size()>0){
                //StringBuilder sb = new StringBuilder();
                //for (EvalExpr e:endResult)
                //	sb.append(e);
                //
                //o.addRow("Precompiled: "+sb);
                Log.d("EXPR","Precompiled: "+endResult.toString());
                return endResult;
            }

        }
        o.e("failed to precompile: "+expression);
        o.e("End Result: "+endResult);
        Log.e("EXPR","failed to precompile: "+expression);
        Log.e("EXPR","End Result: "+endResult);
        printTokens(result);

        return null;
    }

    private static void printTokens(List<Token> expr) {
        //System.out.print(cc+":");
        if (expr !=null) {
            for (Token t : expr) {
                System.out.print(t.str + "[" + t.type.name() + "]");

            }
            System.out.println();
        }
    }

    public static Map<String,String> evaluate(List<Expressor.EvalExpr> eContext,Map<String,String> evalContext) {
        if(eContext == null)
            return null;
        Log.d("EXPR", "in evaluate with context "+(evalContext==null?"null":evalContext.toString()));
        String err = null;
        Map<String, String> keyHash = null;
        boolean  hasWildCard = false;
        Logger o = Logger.gl();
        keyHash = new HashMap<String, String>();
        String cContext = analyze(eContext,evalContext);
        if (cContext==null) {
            err = "Context syntax error when evaluating precompiled context: "+eContext.toString();

        } else {
            String[] pairs = cContext.split(",");
            if (pairs == null || pairs.length == 0) {
                Log.d("EXPR", "Could not split context on comma (,)");
                err = "Could not split context on comma (,). for context " + cContext;

            } else {
                for (String pair : pairs) {
                    //Log.d("nils","found pair: "+pair);
                    if (pair != null && !pair.isEmpty()) {
                        String[] kv = pair.split("=");

                        if (kv == null || kv.length < 2) {
                            err = "Context " + eContext + " cannot be evaluated, likely due to a missing variable value. Evaluation: " + cContext;
                            break;
                        } else {
                            String arg = kv[0].trim();
                            String val = kv[1].trim();
                            //Log.d("nils","Keypair: "+arg+","+val);

                            if (val.isEmpty() || arg.isEmpty()) {
                                err = "Empty key or value in context keypair for context " + cContext;
                                break;
                            }

                            for (char c : val.toCharArray()) {
                                if (!Character.isLetterOrDigit(c) && c != '-' && c != '_' && c != '*') {
                                    if (c == '?') {
                                        hasWildCard = true;
                                    } else {
                                        err = "The literal " + val + " contains non alfabetic-nonnumeric characters. Did you forget braces? [ ] Full context is: " + cContext;
                                        break;
                                    }
                                }
                            }
                            if (err == null)
                                keyHash.put(arg, val);
                            //Log.d("nils","Added "+arg+","+val+" to current context");

                        }
                    }
                }
            }
        }
        if (err!=null) {
            o.e(err);
            return null;
        } else {
            Log.d("EXPR","evaluate returns: "+keyHash+ " for "+eContext+" isPartial "+hasWildCard);
            //if (hasWildCard) {
            //keyHash=GlobalState.getInstance().getDb().createNotNullSelection(keyHash);
            //Log.d("vortex","DB_CONTEXT ACTUALLY returns: "+keyHash+ " for "+eContext+" isPartial "+hasWildCard);
            //}
            return keyHash;
        }

    }

}
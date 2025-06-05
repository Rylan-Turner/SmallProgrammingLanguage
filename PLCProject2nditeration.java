package smallprogramminglanguage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class PLCProject2nditeration{

    //global declarations
    //variables
    static int charClass;
    static char[] lexeme;
    static char nextChar;
    static int lexLen;
    static String token;
    static int nextToken;
    static int line = 1;
    static String beginToken = " ";
    static String declToken;
    static ArrayList<String> symbolTable = new ArrayList<String>();
    static ArrayList<String> redeclaredSymbol = new ArrayList<String>();
    static ArrayList<String> reservedWords = new ArrayList<String>(Arrays.asList("program", "begin", "end", "if", "then", 
                                                                                "else", "input", "output", "int", "while", 
                                                                                "loop"));
    static String inputToken;
    static String outputToken;

    //file reader
    static File in_fp;
    static FileInputStream fileInputStream;
    static InputStreamReader inputStreamReader;

    //character classes
    public static final int LETTER = 0;
    public static final int DIGIT = 1; 
    public static final int UNDERSCORE = 2;
    public static final int DECIMAL = 3;
    public static final int UNKNOWN = 99;
    //token codes
    public static final int INT_LIT = 10;
    public static final int IDENT = 11;
    public static final int ASSIGN_OP = 20;
    public static final int ADD_OP = 21;
    public static final int SUB_OP = 22;
    public static final int MULT_OP = 23;
    public static final int DIV_OP = 24;
    public static final int LEFT_PAREN = 25;
    public static final int RIGHT_PAREN = 26;
    public static final int COMMA = 30;
    public static final int COLON = 32;
    public static final int SEMICOLON = 34;
    public static final int EQUALS = 36;
    public static final int LESS_THAN = 37;
    public static final int GREATER_THAN = 38;
    public static final int NOT_EQUALS = 39;

    public static final int EOF = -1;

    public static void main(String[] args){
        try{
        //open the file for reading and process its contents
        in_fp = new File("TestCases/input1.txt");
        fileInputStream = new FileInputStream(in_fp);
        inputStreamReader = new InputStreamReader(fileInputStream);

        do{
            
            lex();
            program();
            line = line + 1;
            
        }
        while(nextToken != EOF);

        inputStreamReader.close();
        }

        catch (IOException e){
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
    
    public static void program() throws IOException{
        
        token = new String(lexeme).trim();
        if(!token.equals("program") && line == 1){
            System.err.println("Expected program, got: " + token + " on line: " + line);
            System.exit(0);
            // If program is not found at the beginning, error and shut down
         }
        else{
            if(line == 1){
                System.out.println("PROGRAM");
            }
            token = new String(lexeme).trim();
            if (token.equals("begin")){
                beginToken = "begin";
            }
            else if("begin".equals(beginToken)){
                Begin();
            }
            else  if (nextToken == IDENT && line != 1){
                DeclSection();
            }
            else if(!"program".equals(token)){
                System.err.println("Expected ID or begin, got: " + token + " on line: " + line);
                System.exit(0);
            }
        }
    }
    
    private static void Begin() throws IOException{
        if("else".equals(token)){
            // This line here is to parse the else token and go back to the start of the program once again
        }
        else if(!"end".equals(token)){
            StmtSection();
        }
         else if("end".equals(token)){
             End();
        }
    }
    
    private static void End() throws IOException{
        lex();
        token = new String(lexeme).trim();
        if("loop".equals(token)){
            lex();
            if(nextToken != SEMICOLON){
                System.err.println("Missing semicolon on line: " + line);
                System.exit(0);
            }
         }
         else if("if".equals(token)){
            lex();
            if(nextToken != SEMICOLON){
                System.err.println("Missing semicolon on line: " + line);
                System.exit(0);
            }
        }
        else if(nextToken == SEMICOLON){
            lex();
        }
        else{
            System.err.println("Missing semicolon after end on line: " + line);
            System.exit(0);
        }
    }
    
    //getChar - get and determine next character
    static void getChar() throws IOException{
        int nextCharValue;
        // Read the next character from the file
        if ((nextCharValue = inputStreamReader.read()) != EOF) {
            nextChar = (char) nextCharValue;
            if(Character.isAlphabetic(nextChar))
                charClass = LETTER;
            else if (Character.isDigit(nextChar))
                charClass = DIGIT;
            else if (nextChar == '_'){
                charClass = UNDERSCORE;
            }
            else if (nextChar == '.'){
                charClass = DECIMAL;
            }
            else
                charClass = UNKNOWN;
        }
        else{
            charClass = EOF;
        }
    }

    //lex - arithmetic analyzer
    static int lex() throws IOException{
        if(line == 1)
        {
            getChar();
        }
        while(Character.isWhitespace(nextChar))
        {
            getChar();
        }
        lexeme = new char[100];
        lexLen = 0;
        getNonBlank();
        switch(charClass){
            //parse identifiers
            case LETTER:
                addChar();
                getChar();
                while(charClass == UNDERSCORE || charClass == LETTER || charClass == DIGIT){
                    addChar();
                    getChar();
                }
                nextToken = IDENT;
                break;
            case UNDERSCORE:
                addChar();
                getChar();
                while(charClass == UNDERSCORE || charClass == LETTER || charClass == DIGIT){
                    addChar();
                    getChar();
                }
                nextToken = IDENT;
                break;
            //parse integer literals
            case DIGIT:
                addChar();
                getChar();
                while(charClass == DIGIT || charClass == DECIMAL){
                    addChar();
                    getChar();
                }
                nextToken = INT_LIT;
                break;
            //parenthesis and operators
            case UNKNOWN:
                lookup(nextChar);
                getChar();
                break;
            //eof
            case EOF:
                nextToken = EOF;
                lexeme[0] = 'E';
                lexeme[1] = 'O';
                lexeme[2] = 'F';
                lexeme[3] = 0;
                break;
        }
        //end of switch statement
        //System.out.println("Next token is: " + nextToken + ", Next lexeme is " + String.valueOf(lexeme));

        return nextToken;
    }

    //getNonBlank - a function to sift through getChar until it returns 
    //              a non-whitespace character
    static void getNonBlank() throws IOException{
        int count = 0;
        while(Character.isWhitespace(nextChar) && count < 100){
            getChar();
            count++;
        }
    }

    //addChar - a function to add nextChar to lexeme
    static void addChar(){
        if(lexLen <= 98){
            lexeme[lexLen++] = nextChar;
            lexeme[lexLen] = 0;
        }
        else{
            System.out.println("Error - lexeme is too long on line: " + line);
        }
    }

    //lookup - a function to lookup operators and parentheses
    //         and return the token
    static int lookup(char ch){
        switch(ch){
            case '(':
                addChar();
                nextToken = LEFT_PAREN;
                break;
            case ')':
                addChar();
                nextToken = RIGHT_PAREN;
                break;
            case '+':
                addChar();
                nextToken = ADD_OP;
                break;
            case '-':
                addChar();
                nextToken = SUB_OP;
                break;
            case '*':
                addChar();
                nextToken = MULT_OP;
                break;
            case '/':
                addChar();
                nextToken = DIV_OP;
                break;
            case ':':
                addChar();
                nextToken = COLON;
                break;
            case ',':
                addChar();
                nextToken = COMMA;
                break;
            case ';':
                addChar();
                nextToken = SEMICOLON;
                break;
            case '=':
                addChar();
                nextToken = EQUALS;
                break;
            case '<':
                addChar();
                nextToken = LESS_THAN;
                break;
            case '>':
                addChar();
                nextToken = GREATER_THAN;
                break;
            default:
                nextToken = EOF;
                break;
        }
    
        return nextToken;
    }

    private static void DeclSection() throws IOException{
        System.out.println("DECL_SEC");
        declToken = "decl";
        Decl();
    }

    private static void Decl() throws IOException{
        System.out.println("DECL");
        ID_List();
        lex();
        type();
        lex();
        if(nextToken != SEMICOLON){
            System.err.println("Missing semicolon on line: " + line);
            System.exit(0);
        }
    }
    
   private static void ID_List() throws IOException{
   System.out.println("ID_LIST");
        if(nextToken == IDENT || nextToken == INT_LIT){
            if("input".equals(inputToken) || "output".equals(outputToken)){
                SymbolMatch();
            }
            if("decl".equals(declToken)){
                RedeclarationCheck();
            }
            ReservedMatch();
            if(nextToken == INT_LIT){
                 NumberLength();
            }
            token = new String(lexeme).trim();
            symbolTable.add(token);

            lex();
            if(nextToken != SEMICOLON){
                if(nextToken != COLON){
                    if(nextToken == COMMA){
                        lex();
                    }
                    else{
                        System.err.println("Comma, colon, or semicolon expected on line: " + line);
                        System.exit(0);
                    }
                    ID_List();
                 }
             }
        }
        else{
            System.err.println("Illegal symbol or ID on line: " + line);
            System.exit(0);
        }
   }
   
   private static void StmtSection() throws IOException{
   System.out.println("STMT_SECT");
   declToken =" ";
   Stmt();
    }

    private static void Stmt() throws IOException{
    System.out.println("STMT");
    token = new String(lexeme).trim();
        if(token.equals("input")){
            input();
        }
        else if(token.equals("output")){
            output();
        }
        else if(token.equals("if")){
            ifStmt();
        }
        else if(token.equals("while")){
            whileStmt();
        }
        else if(nextToken == IDENT){
            assign();
        }
        else{
            System.err.println("Expected input, output, if, while, or ID. Got: " + token + " on line: " + line);
            System.exit(0);
        }
    }

    private static void input() throws IOException{
    System.out.println("INPUT");
    inputToken = "input";
    lex();
    SymbolMatch();
    ID_List();
    }

    private static void output() throws IOException{
    System.out.println("OUTPUT");
    outputToken = "output";
    lex();
        if(nextToken != INT_LIT){
            SymbolMatch();
            ID_List();
        }
        if(nextToken == INT_LIT){
            lex();
        }
    }

    private static void ifStmt() throws IOException{
    System.out.println("IF_STMT");
    Comp();
        if(nextToken == RIGHT_PAREN){
            lex();
        }
    }
    
    private static void whileStmt() throws IOException{
    System.out.println("WHILE_STMT");
    Comp();
     token = new String(lexeme).trim();
        if("loop".equals(token)){
            line = line + 1;
         }
        else{
            System.err.println("loop missing for while statement on line: " + line);
            System.exit(0);
        }
    }
    
    private static void Comp() throws IOException{
    System.out.println("COMP");
    lex();
        if(nextToken == LEFT_PAREN){
            lex();
            SymbolMatch();
            if(nextToken == IDENT || nextToken == INT_LIT){
                operand();
            }
            else{
                System.err.println("Missing an identifiier or integer for comparison statement on line: " + line);
                System.exit(0);
            }
        }
        else{
            System.err.println("Missing left parenth in comparison statement on line: " + line);
            System.exit(0);
        }
    }

    private static void assign() throws IOException{
    System.out.println("ASSIGN");
    SymbolMatch();
    lex();
    if(nextToken == COLON){
        lex();
        if(nextToken == EQUALS){
            lex();
        }
        else{
            System.err.println("Missing equal sign for assignment statement on line: " + line);
            System.exit(0);
        }
    }
    else{
        System.err.println("IMissing colon for assignment statement on line: " + line);
        System.exit(0);
    }
    expr();
        if(nextToken != SEMICOLON){
            System.err.println("Missing semicolon on line: " + line);
            System.exit(0);
        }
    }

    private static void expr() throws IOException{
    System.out.println("EXPR");
        if(nextToken == LEFT_PAREN){
            lex();
        }
    factor();
    }

    private static void factor() throws IOException{
    System.out.println("FACTOR");
        if(nextToken == LEFT_PAREN){
            lex();
        }
    operand();
    }

    private static void operand() throws IOException{
    System.out.println("OPERAND");
        if(nextToken == IDENT){
            SymbolMatch();
        }
    lex();
        if(nextToken == ADD_OP || nextToken == SUB_OP){
            lex();
            expr();
        }
        if(nextToken == MULT_OP || nextToken == DIV_OP){
            lex();
            factor();
        }
        if(nextToken == LESS_THAN || nextToken == GREATER_THAN || nextToken == EQUALS){
            lex();
            if(nextToken == GREATER_THAN || nextToken == EQUALS){
                lex();
            }
            operand();
        }
        if(nextToken == RIGHT_PAREN){
            lex();
        }
    }

    private static void SymbolMatch() throws IOException{
    token = new String(lexeme).trim();
        if(nextToken == IDENT){
            if(symbolTable.contains(token)){
                //Successful match of a declared variable causes it to leave the if statement
            }
            else{
                System.err.println("Identifier not declared on line: " + line);
                System.exit(0);
            }
        }
    }
    
    private static void ReservedMatch() throws IOException{
    token = new String(lexeme).trim();
        if(nextToken == IDENT){
            if(reservedWords.contains(token)){
                System.err.println("Reserved word: " + token + " can not be used as an identifier on line: " + line);
                System.exit(0);
            }
        }
    }

    private static void type() throws IOException{
    token = new String(lexeme).trim();
        if(!"int".equals(token) && !"float".equals(token) && !"double".equals(token)){
            System.err.println("Invalid type on line: " + line + " valid types are int, float, or double.");
            System.exit(0);
        }
    }
    
    private static void RedeclarationCheck() throws IOException{
    token = new String(lexeme).trim();
        if(redeclaredSymbol.contains(token)){
            System.err.println("Symbol redeclared on line: " + line);
            System.exit(0);
        }
    redeclaredSymbol.add(token);
    }

    private static void NumberLength() throws IOException{
    token = new String(lexeme).trim();
        if(token.length() > 10){
            System.err.println("Integer or decimal is too long on line: " + line);
            System.exit(0);
        }
    }
}
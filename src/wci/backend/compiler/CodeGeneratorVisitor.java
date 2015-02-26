package wci.backend.compiler;

import java.util.ArrayList;

import wci.frontend.*;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.Predefined;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;

public class CodeGeneratorVisitor
    extends CParserVisitorAdapter
    implements CParserTreeConstants
{
    private SymTabStack symTabStack;
    private static int label_count = 0;
    private static String programName = null;
    public CodeGeneratorVisitor(SymTabStack symTabStack) {
        super();
        this.symTabStack = symTabStack;
    }
    public String getCurrentLabel() { return "label" + label_count; }
    public String getNextLabel() { return "label" + ++label_count; }

    public boolean comparisonEmitter(SimpleNode node, Object data) {
        if(node.jjtGetNumChildren() != 2 ) {
            return false;
        }

        SimpleNode left_op = (SimpleNode) node.jjtGetChild(0);
        SimpleNode right_op = (SimpleNode) node.jjtGetChild(1);  

        SimpleNode expr1 = findValuedNode((SimpleNode) left_op.jjtGetChild(0));
        SimpleNode expr2 = findValuedNode((SimpleNode) right_op.jjtGetChild(0));

        SymTabEntry var1 = symTabStack.lookup(expr1.jjtGetValue().toString());
        SymTabEntry var2 = symTabStack.lookup(expr2.jjtGetValue().toString());

        TypeSpec lType = null;
        TypeSpec rType = null;

        if(var1!= null){
        	lType = var1.getTypeSpec();
        }else{
        	lType = expr1.getTypeSpec();
        }

        if(var2!= null ) {
        	rType = var2.getTypeSpec();
        }else{
        	rType = expr2.getTypeSpec();
        }


        if (lType != rType) {
            System.out.println(String.format("Error: Incompatible types: %s, %s\n", lType, rType));
            return false;
        }

        // everything's good, so
        // emit code for left and right operands
        // and convert to longs for easier comparison
        left_op.jjtAccept(this, data);

        if(lType == Predefined.integerType) {
            CodeGenerator.objectFile.println("    i2l");
        }

        right_op.jjtAccept(this, data);

        if(rType == Predefined.integerType) {
            CodeGenerator.objectFile.println("    i2l");
        }

        // emit the appropriate comparison
        if (lType == Predefined.floatType) {
            CodeGenerator.objectFile.println("    fcmpg");
        } else if (lType == Predefined.integerType) {
            CodeGenerator.objectFile.println("    lcmp");
        } else {
            System.out.println(String.format("Unsupported type comparison for type: %s", lType));
            return false;
        }

        return true;
    }

    public Object visit(ASTassignmentStatement node, Object data)
    {
        String programName        = (String) data;
        SimpleNode identNode      = (SimpleNode) node.jjtGetChild(0);
        SimpleNode expressionNode = (SimpleNode) node.jjtGetChild(1);

        // Emit code for the expression.
        expressionNode.jjtAccept(this, data);
        TypeSpec expressionType = expressionNode.getTypeSpec();

        // Get the assignment target type.
        TypeSpec targetType = node.getTypeSpec();

        // Convert an integer value to float if necessary.
        if ((targetType == Predefined.floatType) &&
            (expressionType == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        SymTabEntry id = (SymTabEntry) identNode.getAttribute(ID);
        String fieldName = id.getName();
        TypeSpec type = id.getTypeSpec();
        String typeCode = getTypeCode(type);

        Integer localSlot = (Integer) id.getAttribute(LOCAL_SLOT);
        if (localSlot != null) {
            String typePrefix = getTypePrefix(type);
            CodeGenerator.objectFile.println("    " + typePrefix + "store " + localSlot);
        } else {
            CodeGenerator.objectFile.println("    putstatic " + programName +
                    "/" + fieldName + " " + typeCode);
        }

        // Emit the appropriate store instruction.
        CodeGenerator.objectFile.flush();

        return data;
    }
    public Object visit(ASTmultExpr node, Object data)
    {
        switch(node.jjtGetNumChildren()) {
            // 1 child means it is a recursive definition. Keep
            // recursing
            case 1:
                node.childrenAccept(this, data);
                break;

            // 2 children means this is an actual mult expression
            case 2:
                SimpleNode expr1 = findValuedNode((SimpleNode) node.jjtGetChild(0));
                SimpleNode expr2 = findValuedNode((SimpleNode) node.jjtGetChild(1));

                TypeSpec expr1Type = expr1.getTypeSpec();
                TypeSpec expr2Type = expr2.getTypeSpec();

                if(expr1Type == null){
                	SymTabEntry lvar = (SymTabEntry) expr1.getAttribute(ID);
                    expr1Type = lvar.getTypeSpec();
                }

                if(expr2Type == null){
                	SymTabEntry rvar = (SymTabEntry) expr2.getAttribute(ID);
                    expr2Type = rvar.getTypeSpec();
                }

                // Get the addition type.
                TypeSpec type = expr1Type;


                // Emit the code for the two expressions and do type conversions,
                // if necessary
                expr1.jjtAccept(this, data);
                if ((expr1Type == Predefined.integerType) &&
                        (expr2Type == Predefined.floatType))
                {
                    CodeGenerator.objectFile.println("    i2f");
                    CodeGenerator.objectFile.flush();
                    type = Predefined.floatType;
                }
                
                expr2.jjtAccept(this, data);
                if ((expr1Type == Predefined.floatType) &&
                        (expr2Type == Predefined.integerType)) {
                    CodeGenerator.objectFile.println("    i2f");
                    CodeGenerator.objectFile.flush();
                    type = Predefined.floatType;
                }

                String typePrefix = (type == Predefined.integerType) ? "i" : "f";

                // Emit the appropriate mult instruction.
				String operator;
				if(node.jjtGetValue().toString().equals("*")) operator="mul";
				else if(node.jjtGetValue().toString().equals("/")) operator="div";
				else operator="rem";
                CodeGenerator.objectFile.println("    " + typePrefix + operator);
                CodeGenerator.objectFile.flush();

                break;
        }

        return data;
    }
    public Object visit(ASTaddExpr node, Object data)
    {
        switch(node.jjtGetNumChildren()) {
            // 1 child means it is a recursive definition. Keep
            // recursing
            case 1:
                node.childrenAccept(this, data);
                break;

            // 2 children means this is an actual add expression
            case 2:
                SimpleNode expr1 = findValuedNode((SimpleNode) node.jjtGetChild(0));
                SimpleNode expr2 = findValuedNode((SimpleNode) node.jjtGetChild(1));

                TypeSpec expr1Type = expr1.getTypeSpec();
                TypeSpec expr2Type = expr2.getTypeSpec();

                if(expr1Type == null){
                	SymTabEntry lvar = (SymTabEntry) expr1.getAttribute(ID);
                    expr1Type = lvar.getTypeSpec();
                }
                if(expr2Type == null){
                	SymTabEntry rvar = (SymTabEntry) expr2.getAttribute(ID);
                    expr2Type = rvar.getTypeSpec();
                }
                // Get the addition type.
                TypeSpec type = expr1Type;

                // Emit code for the first expression
                // with type conversion if necessary.
                expr1.jjtAccept(this, data);
                if ((expr1Type == Predefined.integerType) &&
                        (expr2Type == Predefined.floatType))
                {
                    CodeGenerator.objectFile.println("    i2f");
                    CodeGenerator.objectFile.flush();
                    type = Predefined.floatType;
                }

                // Emit code for the second expression
                // with type conversion if necessary.
                expr2.jjtAccept(this, data);
                if ((expr1Type == Predefined.floatType) &&
                        (expr2Type == Predefined.integerType))
                {
                    CodeGenerator.objectFile.println("    i2f");
                    CodeGenerator.objectFile.flush();
                    type = Predefined.floatType;
                }

                String typePrefix = (type == Predefined.integerType) ? "i" : "f";

                // Emit the appropriate add instruction.
				String operator;
				if(node.jjtGetValue().toString().equals("+")) operator="add";
				else operator="sub";

				CodeGenerator.objectFile.println("    " + typePrefix + operator);
                CodeGenerator.objectFile.flush();
                break;
        }
        return data;
    }

    public Object visit(ASTVariableDeclarationExpr node, Object data) {
        // if there is no expression, no code needs to be emitted
        if(node.jjtGetNumChildren() < 3) {
            return data;
        }

        String programName        = (String) data;
        SimpleNode varTypesNode   = (SimpleNode) node.jjtGetChild(0);
        SimpleNode identNode      = (SimpleNode) node.jjtGetChild(1);
        SimpleNode expressionNode = (SimpleNode) node.jjtGetChild(2);

        // Emit code for the expression.
        expressionNode.jjtAccept(this, data);
        TypeSpec expressionType = expressionNode.getTypeSpec();

        // Get the assignment target type.
        TypeSpec targetType = varTypesNode.getTypeSpec();

        // Convert an integer value to float if necessary.
        if ((targetType == Predefined.floatType) &&
            (expressionType == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        SymTabEntry id = (SymTabEntry) identNode.getAttribute(ID);
        String fieldName = id.getName();
        TypeSpec type = id.getTypeSpec();
        String typeCode = getTypeCode(type);
        Integer localSlot = (Integer) id.getAttribute(LOCAL_SLOT);

        // Emit the appropriate store instruction.
        if(localSlot != null) {
            String typePrefix = getTypePrefix(type);
            CodeGenerator.objectFile.println("    " + typePrefix + "store " + localSlot);
        } else {
            CodeGenerator.objectFile.println("    putstatic " + programName +
                    "/" + fieldName + " " + typeCode);
        }

        CodeGenerator.objectFile.flush();

        return data;
    }

    public String getTypeCode(TypeSpec type) {
        if(type == Predefined.integerType) {
            return "I";
        } else if ( type == Predefined.floatType || type == Predefined.doubleType  ) {
            return "F";
        } else if ( type == Predefined.voidType) {
            return "V";
        } else {
            return "Ljava/lang/String;";
        } 
    }

    public String getTypePrefix(TypeSpec type) {
        if(type == Predefined.integerType) {
            return "i";
        } else if ( type == Predefined.floatType || type == Predefined.doubleType  ) {
            return "f";
        } else {
            return "a";
        }
    }

    public SimpleNode findValuedNode(SimpleNode node) {
        Object nodeVal = node.jjtGetValue();
        while(nodeVal == null) {
            node = (SimpleNode) node.jjtGetChild(0);
            nodeVal = node.jjtGetValue();
        }
        return node;
    }

    public Object visit(ASTintLiteral node, Object data)
    {

        int value = (Integer) node.getAttribute(VALUE);
        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc " + value);
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTfloatLiteral node, Object data)
    {
        float value = (Float) node.getAttribute(VALUE);

        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc " + value);
        CodeGenerator.objectFile.flush();

        return data;

    }

    public Object visit(ASTprintStatement node, Object data)
    {
        CodeGenerator.objectFile.println("    getstatic java/lang/System/out Ljava/io/PrintStream;");
        CodeGenerator.objectFile.flush();
        SimpleNode printNode = (SimpleNode) node.jjtGetChild(0);

        while(printNode.getTypeSpec() == null) {
            printNode = (SimpleNode)printNode.jjtGetChild(0);
        }

        TypeSpec type = printNode.getTypeSpec();
        String typeCode = null;
        printNode.jjtAccept(this, data);

        if (type == Predefined.integerType) {
            typeCode = "I";
        }
        else if (type == Predefined.doubleType || type == Predefined.floatType) {
            typeCode = "F";
        }
        else if (type == Predefined.charType) {
            typeCode = "Ljava/lang/String;";
        }


        CodeGenerator.objectFile.println("    invokevirtual java/io/PrintStream/println(" + typeCode + ")V");
        CodeGenerator.objectFile.flush();

        return data;
    }
    public Object visit(ASTcompoundStatement node, Object data){

         node.childrenAccept(this, data);
         try{

         String label = (String) data;
         if(!label.equals(programName)){
         CodeGenerator.objectFile.println("    goto " + label);
         CodeGenerator.objectFile.flush();}
         }catch(Exception e){

         }
        return data;
    }

    public Object visit (ASTifStatement node, Object data){
        SimpleNode condition = (SimpleNode) node.jjtGetChild(0);
        SimpleNode compound = (SimpleNode) node.jjtGetChild(1);
        SimpleNode elseStatement = null;


        if (node.jjtGetNumChildren() == 3) {
            elseStatement = (SimpleNode) node.jjtGetChild(2);
        }

        while(condition.jjtGetNumChildren() != 2){
            condition = (SimpleNode)condition.jjtGetChild(0);
        }
        String label_false = (String) condition.jjtAccept(this, data);

        String label_true = getNextLabel(); 
        compound.jjtAccept(this, label_true); 
        CodeGenerator.objectFile.println(label_false + ":"); 
        CodeGenerator.objectFile.flush();

        if (elseStatement != null) {
            elseStatement.jjtAccept(this, data);
        }

        CodeGenerator.objectFile.println(label_true + ":"); 
        CodeGenerator.objectFile.flush();

        return data;
    }
    public Object visit (ASTwhileStatement node, Object data){
        SimpleNode condition = (SimpleNode) node.jjtGetChild(0);
        SimpleNode compound = (SimpleNode) node.jjtGetChild(1);
        while(condition.jjtGetNumChildren() != 2){
            condition = (SimpleNode)condition.jjtGetChild(0);
        }
        String beginLabel = getNextLabel();
        CodeGenerator.objectFile.println(beginLabel + ":"); // While condition is true, return here
        CodeGenerator.objectFile.flush();
        String endLabel = (String) condition.jjtAccept(this, data); // Restart loop 
        compound.jjtAccept(this, data); //Generate compound code
        CodeGenerator.objectFile.println("    goto " + beginLabel); // Restart loop
        CodeGenerator.objectFile.println(endLabel + ":"); // Jump here when the loop is done
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit (ASTequalityExpr node, Object data){
    if(comparisonEmitter(node, data)){
      String label = getNextLabel();
      String eq_type = (String)node.jjtGetValue();
      if(eq_type.equals("==")) CodeGenerator.objectFile.println("    ifne " + label);
      else if(eq_type.equals("!=")) CodeGenerator.objectFile.println("    ifeq " + label);
      CodeGenerator.objectFile.flush();
      return label;
    }else{
        node.childrenAccept(this, data);
        return data;
    }
  }

   public Object visit(ASTrelationExpr node, Object data){
      if(comparisonEmitter(node, data)){
          String label = getNextLabel();
          String eq_type = (String)node.jjtGetValue();
          if(eq_type.equals("<")) CodeGenerator.objectFile.println("    ifge " + label);
          else if(eq_type.equals(">")) CodeGenerator.objectFile.println("    ifle " + label);
          else if(eq_type.equals("<=")) CodeGenerator.objectFile.println("    ifgt " + label);
          else if(eq_type.equals(">=")) CodeGenerator.objectFile.println("    iflt " + label);
          CodeGenerator.objectFile.flush();
          return label;
       } else {
           node.childrenAccept(this, data);
           return data;
       }
   }

   public Object visit(ASTstatementList node, Object data) {
       if (this.programName == null) {
           this.programName = (String) data;
       }

       return node.childrenAccept(this, data);
   }

    public Object visit(ASTfunctionCallExpr node, Object data) {
        if(node.jjtGetNumChildren() < 2) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(0);
            if(!(child instanceof ASTIdentifier)) {
                child.jjtAccept(this, data);
                return data;
            }
        }

        String programName = (String) data;

        SimpleNode identNode = (SimpleNode) node.jjtGetChild(0);
        SimpleNode exprListNode = null;

        if(node.jjtGetNumChildren() > 1) {
            exprListNode = (SimpleNode) node.jjtGetChild(1);
        }

        String funcName = (String) identNode.jjtGetValue();
        if(funcName != null && funcName.equals("printf")) {
            printf(exprListNode, data);
            return data;
        }

        // else emit code for the defined function
        SymTabEntry funcId = (SymTabEntry) node.getAttribute(ID);
        ArrayList<SymTabEntry> params = (ArrayList<SymTabEntry>) funcId.getAttribute(ROUTINE_PARMS);
        String paramCodes = generateParamCodeList(params);

        TypeSpec funcReturnType = funcId.getTypeSpec();
        String returnCode = getTypeCode(funcReturnType);

        // visit the expressions, if there are any
        if(exprListNode != null) {
            exprListNode.jjtAccept(this, data);
        }

        CodeGenerator.objectFile.
            printf(
                "    invokestatic %s/%s(%s)%s\n",
                programName, funcName,
                paramCodes, returnCode
            );

        return data;
    }

    private String generateParamCodeList(ArrayList<SymTabEntry> params) {
        if (params == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (SymTabEntry id : params) {
            TypeSpec type = id.getTypeSpec();
            String typeCode = CodeGenerator.typeSpecToJasmin.get(type.getIdentifier().getName());
            sb.append(typeCode);
        }
        return sb.toString();
    }

    public void printf(SimpleNode exprListNode, Object data) {
        CodeGenerator.objectFile.println("    getstatic     java/lang/System/out Ljava/io/PrintStream;");
        exprListNode.childrenAccept(this, data);
        TypeSpec type = exprListNode.getTypeSpec();
        CodeGenerator.objectFile.println("    invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V");
        CodeGenerator.objectFile.flush();
    }

    public Object visit(ASTString node, Object data) {
        String value = (String) node.jjtGetValue();

        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc \"" + value + "\"");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTCharacter node, Object data) {
        Character value = (Character) node.jjtGetValue();

        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc \"" + value + "\"");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTIdentifier node, Object data) {
        String programName = (String) data;
        SymTabEntry variableId = (SymTabEntry) node.getAttribute(ID);
        if (variableId != null) {
            String fieldName = variableId.getName();
            TypeSpec type = variableId.getTypeSpec();
            String typeCode = null;

            if (type == Predefined.integerType) {
                typeCode = "I";
            }
            else if (type == Predefined.doubleType || type == Predefined.floatType) {
                typeCode = "F";
            }
            else if (type == Predefined.charType) {
                typeCode = "Ljava/lang/String;";
            }

            SimpleNode parent = (SimpleNode) node.jjtGetParent();

            // if the identifier's parent is NOT a function call,
            // then emit the loading code
            if (typeCode != null && !(parent instanceof ASTfunctionCallExpr)) {
                // Emit a load constant instruction.
                // Emit the appropriate store instruction.
                Integer localSlot = (Integer) variableId.getAttribute(LOCAL_SLOT);
                String typePrefix = getTypePrefix(type);
                CodeGenerator.objectFile.println("    " + typePrefix + "load " + localSlot);
            }
        }

        return data;
    }

    /*
    public Object visit(ASTintegerConstant node, Object data)
    {
        int value = (Integer) node.getAttribute(VALUE);

        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc " + value);
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTrealConstant node, Object data)
    {
        float value = (Float) node.getAttribute(VALUE);

        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc " + value);
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTadd node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        // Emit code for the first expression
        // with type conversion if necessary.
        addend0Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
            (type0 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit code for the second expression
        // with type conversion if necessary.
        addend1Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
            (type1 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit the appropriate add instruction.
        CodeGenerator.objectFile.println("    " + typePrefix + "add");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTsubtract node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        // Emit code for the first expression
        // with type conversion if necessary.
        addend0Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
            (type0 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit code for the second expression
        // with type conversion if necessary.
        addend1Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
            (type1 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit the appropriate add instruction.
        CodeGenerator.objectFile.println("    " + typePrefix + "sub");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(AST"C:\Program Files\Java\jdk1.7.0_40\bin\java" -Didea.launcher.port=7539 "-Didea.launcher.bin.path=C:\Program Files (x86)\JetBrains\IntelliJ IDEA 13.1.3\bin" -Dfile.encoding=UTF-8 -classpath "C:\Program Files\Java\jdk1.7.0_40\jre\lib\charsets.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\deploy.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\javaws.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\jce.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\jfr.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\jfxrt.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\jsse.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\management-agent.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\plugin.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\resources.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\rt.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\ext\access-bridge-64.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\ext\dnsns.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\ext\jaccess.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\ext\localedata.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\ext\sunec.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\ext\sunjce_provider.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\ext\sunmscapi.jar;C:\Program Files\Java\jdk1.7.0_40\jre\lib\ext\zipfs.jar;C:\Users\Kerfuffle\Repositories\final1\out\production\final1;C:\Program Files (x86)\JetBrains\IntelliJ IDEA 13.1.3\lib\idea_rt.jar" com.intellij.rt.execution.application.AppMain wci.frontend.CParser test\addExpr.c
>program
> main
>  compoundStatement
>   statementList
>    VariableDeclaration
>     VariableTypes: int
>     Identifier: i
>    assignmentStatement
>     Identifier: i
>     Expression
>      functionCallExpr
>       assignmentExpr
>        conditionalExpr
>         logicalOrExpr
>          logicalAndExpr
>           equalityExpr
>            relationExpr
>             addExpr: -
>              multExpr
>               unaryExpr
>                postfixExpr
>                 primaryExpr
>                  constant
>                   intLiteral: 4
>              multExpr
>               unaryExpr
>                postfixExpr
>                 primaryExpr
>                  constant
>                   intLiteral: 3
>    functionCallStatement
>     functionCallExpr
>      printStatement
>       String: 4-3=
>    functionCallStatement
>     functionCallExpr
>      printStatement
>       Identifier: i
>    assignmentStatement
>     Identifier: i
>     Expression
>      functionCallExpr
>       assignmentExpr
>        conditionalExpr
>         logicalOrExpr
>          logicalAndExpr
>           equalityExpr
>            relationExpr
>             addExpr: +
>              multExpr
>               unaryExpr
>                postfixExpr
>                 primaryExpr
>                  constant
>                   intLiteral: 4
>              multExpr
>               unaryExpr
>                postfixExpr
>                 primaryExpr
>                  constant
>                   intLiteral: 2
>    functionCallStatement
>     functionCallExpr
>      printStatement
>       String: 4+2=
>    functionCallStatement
>     functionCallExpr
>      printStatement
>       Identifier: i
>    jumpStatement
>     returnExpr
>      Expression
>       functionCallExpr
>        assignmentExpr
>         conditionalExpr
>          logicalOrExpr
>           logicalAndExpr
>            equalityExpr
>             relationExpr
>              addExpr
>               multExpr
>                unaryExpr
>                 postfixExpr
>                  primaryExpr
>                   constant
>                    intLiteral: 0

===== CROSS-REFERENCE TABLE =====

*** PROGRAM main ***

Identifier       Line numbers    Type specification
----------       ------------    ------------------
i                002 003 003 005 006 006 008
                                 Defined as: variable
                                 Scope nesting level: 2
                                 Type form = scalar, Type id = int

Process finished with exit code 0
multiply node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        // Emit code for the first expression
        // with type conversion if necessary.
        addend0Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
            (type0 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit code for the second expression
        // with type conversion if necessary.
        addend1Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
            (type1 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit the appropriate add instruction.
        CodeGenerator.objectFile.println("    " + typePrefix + "mul");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTdivide node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        // Emit code for the first expression
        // with type conversion if necessary.
        addend0Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
            (type0 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit code for the second expression
        // with type conversion if necessary.
        addend1Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
            (type1 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit the appropriate add instruction.
        CodeGenerator.objectFile.println("    " + typePrefix + "div");
        CodeGenerator.objectFile.flush();

        return data;
    }
    */
}

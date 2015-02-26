package wci.intermediate;

import wci.frontend.*;

public class CParserVisitorAdapter implements CParserVisitor
{
    public Object visit(SimpleNode node, Object data)
    {
        return node.childrenAccept(this, data);
    }
    
    public Object visit(ASTcompoundStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }
    
    public Object visit(ASTassignmentStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTassignmentExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTconditionalExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTlogicalOrExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }
    
    public Object visit(ASTlogicalAndExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }
    
    public Object visit(ASTequalityExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTrelationExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTaddExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTmultExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTunaryExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTpostfixExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTprimaryExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTconstant node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTliteralExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTwhileStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTifStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTforStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTjumpStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTemptyStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTreturnExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTswitchStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTstatementList node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTcaseExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTcaseBlock node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTfunctionDefinition node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTfunctionCallExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTprintStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTfunctionCallStatement node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTmain node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTprogram node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTVariableDeclarationList node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTVariableDeclaration node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTVariableDeclarationExpr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTVariableTypes node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTExpression node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTexprList node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTString node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTCharacter node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTintLiteral node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTfloatLiteral node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTNumber node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTErr node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTsingleToken node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTReservedWord node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTSpecialSymbol node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTIdentifier node, Object data)
    {
        return node.childrenAccept(this, data);
    }
}

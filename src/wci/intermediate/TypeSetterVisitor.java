package wci.intermediate;

import wci.frontend.*;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.*;

public class TypeSetterVisitor extends CParserVisitorAdapter
{
    // we're going to need to add strings and chars
    private void setType(SimpleNode node)
    {
        int count = node.jjtGetNumChildren();
        TypeSpec type = Predefined.integerType;
        
        for (int i = 0; (i < count) && (type == Predefined.integerType); ++i) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            TypeSpec childType = child.getTypeSpec();
            
            if (childType == Predefined.floatType) {
                type = Predefined.floatType;
            }
        }
        
        node.setTypeSpec(type);
    }
    
    public Object visit(ASTassignmentStatement node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTassignmentExpr node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTaddExpr node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTmultExpr node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }
    
    public Object visit(ASTVariableDeclaration node, Object data)
    {
        return data;
    }
    
    public Object visit(ASTintLiteral node, Object data)
    {
        return data;
    }
    
    public Object visit(ASTfloatLiteral node, Object data)
    {
        return data;
    }
}

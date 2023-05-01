package Lox;


import java.util.List;


class Interpreter implements Expr.Visitor<Object>,
        Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (Object) ((double)left > (double)right);
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (Object) ((double)left >= (double)right);
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (Object) ((double)left < (double)right);
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (Object) ((double)left <= (double)right);
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (Object) ((double)left + (double)right);
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                if(left instanceof  String && right instanceof Double) {
                    right = Double.toString((Double) right);
                    if(((String) right).endsWith(".0")) {
                        int remove_dec = ((String) right).length() - 2;
                        right = ((String) right).substring(0, remove_dec);
                    }
                    return (String) left + (String) right;
            }
                if(left instanceof  Double && right instanceof String) {
                    left = Double.toString((Double) left);
                    if(((String) left).endsWith(".0")) {
                        int remove_dec = ((String) left).length() - 2;
                        left = ((String) left).substring(0, remove_dec);
                    }
                    return (String) left + (String) right;
                }
            case MINUS:
                if(left instanceof Double && right instanceof Double){
                    return (Object) ((Double) left / (Double) right);
                }

                if(left instanceof Double && right instanceof String){
                    int orginal_len = ((String) right).length();
                    int to_remove = Double.valueOf((Double)left).intValue();

                    if(to_remove > orginal_len)
                        throw new RuntimeError(expr.operator, "Inger part should be smaller than String length");

                    return  ((String) right).substring(to_remove, orginal_len);
                }
                if(left instanceof String && right instanceof Double){
                    int orginal_len = ((String) left).length();
                    int to_remove = Double.valueOf((Double)right).intValue();

                    if(to_remove > orginal_len)
                        throw new RuntimeError(expr.operator, "Inger part should be smaller than String length");

                    return  ((String) left).substring(0, orginal_len - to_remove);
                }
                throw new RuntimeError(expr.operator, "Operand must be a number or string.");
            case SLASH:
                if(left instanceof Double && right instanceof Double){
                    return (Object) ((Double) left / (Double) right);
                }
                throw new RuntimeError(expr.operator, "Operands must be numbers.");
            case STAR:
                if(left instanceof Double && right instanceof Double){
                    return (Object) ((Double) left * (Double) right);
                }

                    if(left instanceof Double && right instanceof String){
                        String value = (String) right;
                        for(int i = 1; i < (Double)left; i++){
                            right += value;
                        }
                        return  (String) right;
                }
                if(left instanceof String && right instanceof Double){
                    String value = (String) left;
                    for(int i = 1; i < (Double)right; i++){
                        left += value;
                    }
                    return  (String) left;
                }
                throw new RuntimeError(expr.operator, "Operand must be a number or string.");
            case BANG_EQUAL: return (Object) !isEqual(left, right);
            case EQUAL_EQUAL: return (Object) isEqual(left, right);
        }

        // Unreachable.
        return null;
    }

    private void checkNumberOperands(Token operator,
                                     Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                return  -(double)right;
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements,
                      Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }
}

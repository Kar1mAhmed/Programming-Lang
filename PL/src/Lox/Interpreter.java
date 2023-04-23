package Lox;

class Interpreter implements Expr.Visitor<Object> {


    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
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
                checkNumberOperands(expr.operator, left, right);
                return (Object) ((double)left - (double)right);
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if((Double) right == 0.0) throw new RuntimeError(expr.operator, "Can't divide by ZERO.");
                return (Object) ((double)left / (double)right);
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
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
}

package Tools;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
public class Main {
    public static void main(String[] args) throws IOException {

        GenerateAst test = new GenerateAst();

        test.main(Arrays.asList("E:\\Coding\\JAVA\\PL\\src\\LOX").toArray(new String[0]));

    }
}
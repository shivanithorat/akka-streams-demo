import java.util.stream.Stream;

public class Java8Streams {

    public static void main(String[] args) {
        Stream<Integer> integerStream = Stream.of(1, 2, 3);
        integerStream.forEach(System.out::println);
        Stream<String> stringStream = integerStream.map(i -> "a" + i);
        stringStream.forEach(System.out::println);
    }
}

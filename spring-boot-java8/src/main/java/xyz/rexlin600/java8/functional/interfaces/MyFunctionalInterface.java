package xyz.rexlin600.java8.functional.interfaces;

/**
 * MyFunctionalInterface 类
 * <p>
 * <p>
 * 自定义一个 Functional Interfaces，接受一个 T 的参数，返回 R 类型数据
 * 使用上类似 {@link java.util.function.BiConsumer}，这里就不再介绍了
 *
 * @author: hekunlin
 * @since: 2020/1/9
 */
@FunctionalInterface
interface MyFunctionalInterface<T, R> {

    /**
     * 接受一个呃呃 T 类型的参数的函数式接口
     *
     * @param t
     * @return
     */
    R apply(T t);

}
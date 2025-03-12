package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLiteManager;

/**
 * DDL构建器基类。
 * 提供数据库结构操作的基础功能。
 *
 * @param <T> 实体类型
 * @param <B> 构建器类型
 */
public abstract class BaseDDLBuilder<T, B extends BaseDDLBuilder<T, B>> {

    /** 表引用映射 */
    protected final Map<String, List<String>> tableRefMap = new HashMap<>();
    /** 实体类列表 */
    protected final List<Class<?>> entityClasses = new ArrayList<>();
    /** 创建表SQL映射 */
    protected final Map<String, List<String>> sqlMap = new HashMap<>();
    /** SQL语句列表 */
    protected List<String> sqls = new ArrayList<>();

    /**
     * 构造函数。
     *
     * @param entityClass 实体类
     */
    protected BaseDDLBuilder(Class<T> entityClass) {
        if (entityClass != null) {
            this.entityClasses.add(entityClass);
        }
    }

    /**
     * 添加单个实体类。
     *
     * @param entityClass 实体类
     * @return 当前构建器实例
     */
    @SuppressWarnings("unchecked")
    public B add(Class<?> entityClass) {
        this.entityClasses.add(entityClass);
        return (B) this;
    }

    /**
     * 添加多个实体类。
     *
     * @param entityClasses 实体类集合
     * @return 当前构建器实例
     */
    @SuppressWarnings("unchecked")
    public B addAll(Collection<Class<?>> entityClasses) {
        if (entityClasses != null) {
            this.entityClasses.addAll(entityClasses);
        }
        return (B) this;
    }

    /**
     * 构建SQL语句。
     *
     * @return 当前构建器实例
     */
    @SuppressWarnings("unchecked")
    public B build() {
        // 初始化SQL映射
        sqlMap.clear();
        tableRefMap.clear();

        // 处理实体类
        processBuild();

        return (B) this;
    }

    /**
     * 处理构建逻辑，由子类实现。
     */
    protected abstract void processBuild();

    /**
     * 执行SQL语句。
     *
     * @return 执行结果
     */
    public Integer execute() {
        return sqls.stream()
            .mapToInt(sql -> (Integer) SQLiteManager.executeSafeSQL(sql))
            .sum();
    }

    /**
     * 根据表排序结果合并SQL语句。
     *
     * @param sortedTables 排序后的表名列表
     * @return 合并后的SQL语句列表
     */
    protected List<String> mergeSqlByOrder(List<String> sortedTables) {
        List<String> result = new ArrayList<>();
        for (String tableName : sortedTables) {
            List<String> sqls = sqlMap.get(tableName);
            if (sqls != null) {
                result.addAll(sqls);
            }
        }
        return result;
    }

    /**
     * 对表进行拓扑排序，处理依赖关系。
     *
     * @return 排序后的表名列表
     */
    protected List<String> sortTablesByDependencies() {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> graph = buildDependencyGraph(inDegree);
        return performTopologicalSort(graph, inDegree);
    }

    /**
     * 构建依赖图
     *
     * @param inDegree 用于存储各节点入度的映射
     * @return 依赖图
     */
    protected Map<String, List<String>> buildDependencyGraph(Map<String, Integer> inDegree) {
        Map<String, List<String>> graph = new HashMap<>();

        // 初始化入度和邻接表
        for (String tableName : sqlMap.keySet()) {
            inDegree.putIfAbsent(tableName, 0);
            graph.putIfAbsent(tableName, new ArrayList<>());
        }

        // 构建依赖关系
        for (Map.Entry<String, List<String>> entry : tableRefMap.entrySet()) {
            String table = entry.getKey();
            graph.putIfAbsent(table, new ArrayList<>());

            for (String dependent : entry.getValue()) {
                inDegree.putIfAbsent(dependent, 0);
                graph.putIfAbsent(dependent, new ArrayList<>());
                graph.get(table)
                    .add(dependent);
                inDegree.put(dependent, inDegree.get(dependent) + 1);
            }
        }

        return graph;
    }

    /**
     * 执行拓扑排序
     *
     * @param graph    依赖图
     * @param inDegree 节点入度映射
     * @return 排序后的表名列表
     */
    protected List<String> performTopologicalSort(Map<String, List<String>> graph, Map<String, Integer> inDegree) {
        List<String> sortedTables = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();

        // 将入度为0的节点加入队列
        inDegree.entrySet()
            .stream()
            .filter(e -> e.getValue() == 0)
            .map(Map.Entry::getKey)
            .forEach(queue::offer);

        while (!queue.isEmpty()) {
            String table = queue.poll();
            sortedTables.add(table);

            for (String dependent : graph.get(table)) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) {
                    queue.offer(dependent);
                }
            }
        }

        // 检查循环依赖
        if (sortedTables.size() != inDegree.size()) {
            List<String> cyclePath = findCycle(graph);
            String errorMessage = cyclePath.isEmpty() ? "检测到循环依赖关系,请检查表之间的外键引用"
                : "检测到循环依赖关系,循环路径为: " + String.join(" -> ", cyclePath) + " -> " + cyclePath.get(0);
            throw new IllegalStateException(errorMessage);
        }

        return sortedTables;
    }

    /**
     * 查找依赖图中的循环
     *
     * @param graph 依赖图
     * @return 循环路径，如果没有循环则返回空列表
     */
    protected List<String> findCycle(Map<String, List<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String table : graph.keySet()) {
            if (!visited.contains(table)) {
                List<String> cyclePath = new ArrayList<>();
                if (detectCycle(table, graph, visited, recursionStack, cyclePath)) {
                    return cyclePath;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 使用DFS检测循环
     *
     * @param table          当前表
     * @param graph          依赖图
     * @param visited        已访问节点集合
     * @param recursionStack 当前递归栈
     * @param cyclePath      用于存储检测到的循环路径
     * @return 是否检测到循环
     */
    protected boolean detectCycle(String table, Map<String, List<String>> graph, Set<String> visited,
        Set<String> recursionStack, List<String> cyclePath) {
        visited.add(table);
        recursionStack.add(table);

        for (String dependent : graph.get(table)) {
            if (!visited.contains(dependent)) {
                if (detectCycle(dependent, graph, visited, recursionStack, cyclePath)) {
                    cyclePath.add(0, table);
                    return true;
                }
            } else if (recursionStack.contains(dependent)) {
                cyclePath.add(0, table);
                return true;
            }
        }

        recursionStack.remove(table);
        return false;
    }
}

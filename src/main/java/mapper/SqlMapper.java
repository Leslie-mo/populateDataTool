package mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

@Mapper
public interface SqlMapper {

    static class PureSqlProvider {
        public String sql(String sql) {
            return sql;
        }
    }

    @SelectProvider(type = PureSqlProvider.class, method = "sql")
    public List<Map<String, String>> selectList(String sql);

    @SelectProvider(type = PureSqlProvider.class, method = "sql")
    public Map<String, String> selectOne(String sql);

}

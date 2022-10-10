package item1

data class User(val name: String)

class UserRepository {
    private val storedUsers: MutableMap<Int, String> = mutableMapOf()
    fun loadAll(): MutableMap<Int, String> = storedUsers
}

class UserRepositoryRenew {
    private val storedUsers: MutableMap<Int, String> = mutableMapOf()
    fun loadAll(): Map<Int, String> = storedUsers // 읽기 전용 슈퍼타입으로 업캐스팅하여 가변성을 제한함
}

fun main() {
    val repository = UserRepository()
    val storedUsers = repository.loadAll()
    storedUsers[2] = "aaaaaaaa" // private 상태인 UserRepository.storedUsers를 수정하는 불상사가 발생해버림

    val repositoryRenew = UserRepositoryRenew()
    val renewStoredUsers = repositoryRenew.loadAll()
//    renewStoredUsers[2] = "bbbbbbbb" // 컴파일 에러(No set method providing array access)
}
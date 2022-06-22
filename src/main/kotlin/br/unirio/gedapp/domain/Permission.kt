package br.unirio.gedapp.domain

enum class Permission(val level: PermissionLevel) {
    SEARCH_DOCS         (PermissionLevel.DEFAULT),
    ADD_DOCS            (PermissionLevel.DEPARTMENT),
    EDIT_DOCS_OTHERS    (PermissionLevel.DEPARTMENT),
    DELETE_DOCS_OTHERS  (PermissionLevel.DEPARTMENT),
    INVITE_USERS        (PermissionLevel.DEPARTMENT),
    MANAGE_CATEGORIES   (PermissionLevel.DEPARTMENT),
    MANAGE_DEPT_PERM    (PermissionLevel.DEPARTMENT),
    MANAGE_DEPARTMENTS  (PermissionLevel.SYSTEM),
    MANAGE_SYSTEM_PERM  (PermissionLevel.SYSTEM);

    companion object {
        fun getPermissionsByLevel(level: PermissionLevel) = values().filter { it.level == level }

        fun getDefaultPermissions() = getPermissionsByLevel(PermissionLevel.DEFAULT)

        fun getDepartmentPermissions() = getPermissionsByLevel(PermissionLevel.DEPARTMENT)

        fun getSystemPermissions() =getPermissionsByLevel(PermissionLevel.SYSTEM)
    }
}
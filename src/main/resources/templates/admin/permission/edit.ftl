<#import "../../layout/defaultLayout.ftl" as layout>
<@layout.layout "Edit permission">
   <h1>Edit permission</h1>

   <form method="post" action="${urlFor('update?id=' + permission.id)}">
     <#include "_form.ftl">
     <button type="submit" class="btn btn-primary">Update</button>
   </form>
</@layout.layout>

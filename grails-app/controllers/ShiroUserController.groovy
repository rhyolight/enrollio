import org.apache.shiro.crypto.hash.Sha1Hash

class ShiroUserController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ shiroUserInstanceList: ShiroUser.list( params ), shiroUserInstanceTotal: ShiroUser.count() ]
    }

    def show = {
        def shiroUserInstance = ShiroUser.get( params.id )

        if(!shiroUserInstance) {
            flash.message = "ShiroUser not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ shiroUserInstance : shiroUserInstance ] }
    }

    def delete = {
        def shiroUserInstance = ShiroUser.get( params.id )
        if(shiroUserInstance) {
            try {
                shiroUserInstance.delete(flush:true)
                flash.message = "ShiroUser ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "ShiroUser ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "ShiroUser not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def shiroUserInstance = ShiroUser.get( params.id )

        if(!shiroUserInstance) {
            flash.message = "ShiroUser not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ shiroUserInstance : shiroUserInstance ]
        }
    }

    def update = {
        def shiroUserInstance = ShiroUser.get( params.id )
        if(shiroUserInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(shiroUserInstance.version > version) {
                    
                    shiroUserInstance.errors.rejectValue("version", "shiroUser.optimistic.locking.failure", "Another user has updated this ShiroUser while you were editing.")
                    render(view:'edit',model:[shiroUserInstance:shiroUserInstance])
                    return
                }
            }
            shiroUserInstance.properties = params
            if(!shiroUserInstance.hasErrors() && shiroUserInstance.save()) {
                flash.message = "ShiroUser ${params.id} updated"
                redirect(action:show,id:shiroUserInstance.id)
            }
            else {
                render(view:'edit',model:[shiroUserInstance:shiroUserInstance])
            }
        }
        else {
            flash.message = "ShiroUser not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def shiroUserInstance = new ShiroUser()
        shiroUserInstance.properties = params
        return ['shiroUserInstance':shiroUserInstance]
    }

    def save = {
        def shiroUserInstance = new ShiroUser(params)
        if(!shiroUserInstance.hasErrors()) {
            // We know that the password and password confirm are equal.
            // Generate a new password hash
            shiroUserInstance.passwordHash = new Sha1Hash(shiroUserInstance.password).toHex()
            shiroUserInstance.save()
            flash.message = "ShiroUser ${shiroUserInstance.id} created"
            redirect(action:show,id:shiroUserInstance.id)
        }
        else {
            render(view:'create',model:[shiroUserInstance:shiroUserInstance])
        }
    }
}

from locust import HttpUser, task, between

class OwnerBehavior(HttpUser):
    wait_time = between(1, 2.5)

    @task(2)
    def read_owner(self):
        self.client.get("/owners/1")

    @task(1)
    def read_all_owners(self):
        self.client.get("/owners")

    @task(1)
    def create_owner_2(self):
        self.client.post("/owners", json={
            "firstName": "Alice",
            "lastName": "Smith",
            "address": "456 Main St",
            "city": "Springfield",
            "telephone": "5555555556"
        })

    @task(2)
    def read_owner_2(self):
        self.client.get("/owners/2")

    @task(1)
    def create_owner_3(self):
        self.client.post("/owners", json={
            "firstName": "Charlie",
            "lastName": "Brown",
            "address": "789 Main St",
            "city": "Springfield",
            "telephone": "5555555557"
        })

    @task(2)
    def read_owner_3(self):
        self.client.get("/owners/3")

class WebsiteUser(OwnerBehavior):
    pass
